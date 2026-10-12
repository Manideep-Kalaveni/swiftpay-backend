package com.manideep.swiftpay.operations_service.settlement;

import com.manideep.swiftpay.common_lib.dto.PaymentSettlementView;
import com.manideep.swiftpay.common_lib.dto.SettlementBankDetailsDTO;
import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.common_lib.enums.EventAggregateType;
import com.manideep.swiftpay.common_lib.enums.SettlementStatus;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.operations_service.entity.Settlement;
import com.manideep.swiftpay.operations_service.entity.SettlementPayment;
import com.manideep.swiftpay.operations_service.entity.SettlementPaymentId;
import com.manideep.swiftpay.operations_service.outbox.OutboxEventPublisher;
import com.manideep.swiftpay.operations_service.repository.SettlementPaymentRepository;
import com.manideep.swiftpay.operations_service.repository.SettlementRepository;
import com.manideep.swiftpay.operations_service.settlement.dto.BankTransferResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementTransactionExecutor {

    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settlementPaymentRepository;
    private final BankTransferProcessor bankTransferProcessor;
    private final OutboxEventPublisher outboxEventPublisher;
    private final SettlementIntegrationGateway settlementIntegrationGateway;
    // OutboxEventPublisher inject it.
    private static final double GST_RATE = 0.18;
    private static final double FEE_RATE = 0.02;

    @Transactional
    public void processForMerchant(UUID merchantId, LocalDate settlementDate){
        List<PaymentSettlementView> unsettledPayments = settlementIntegrationGateway.findUnsettledCapturedPayments(merchantId);

        if(unsettledPayments.isEmpty()) return;

        log.info("Processing {} unsettled payments for merchant id {}", unsettledPayments.size(), merchantId);
        Integer grossAmount = unsettledPayments.stream()
                .map(PaymentSettlementView::amountUnits)
                .reduce(Integer::sum)
                .orElse(0);

        Money gross = Money.of(grossAmount,unsettledPayments.getFirst().currency());

        int fee = Math.toIntExact(Math.round(gross.getAmountUnits() * FEE_RATE));
        int gst = Math.toIntExact(Math.round(fee * GST_RATE));

        Money feeAmount = Money.of(fee, gross.getCurrency());
        Money gstAmount = Money.of(gst, gross.getCurrency());
        Money netAmount = gross.subtract(feeAmount).subtract(gstAmount);

        Settlement settlement = Settlement.builder()
                .merchantId(merchantId)
                .status(SettlementStatus.INITIATED)
                .feeAmount(feeAmount)
                .grossAmount(gross)
                .netAmount(netAmount)
                .gstAmount(gstAmount)
                .build();

        settlementRepository.save(settlement);

        try{
            List<SettlementPayment> links = new ArrayList<>();
            for(PaymentSettlementView payment : unsettledPayments){
                links.add(SettlementPayment.builder()
                        .id(new SettlementPaymentId(settlement.getId(),payment.paymentId()))
                        .settlement(settlement)
                        .build());
            }

            settlementPaymentRepository.saveAll(links);

//            List<UUID> paymentIds = unsettledPayments.stream().map(payment -> payment.paymentId()).toList();
//            paymentServiceClient.markSettled(paymentIds);

            SettlementBankDetailsDTO settlementBankDetails = settlementIntegrationGateway.getSettlementBankDetails(merchantId);
            // Call the bank transfer api ...
            BankTransferResult bankTransferResult = bankTransferProcessor.initiate(settlement.getId(),merchantId,netAmount,settlementBankDetails.accountNumber(),settlementBankDetails.ifsc());

            settlement.setStatus(SettlementStatus.TRANSFER_PENDING);
            settlement.setBankReference(bankTransferResult.bankRef());

            settlementRepository.save(settlement);
        }
        catch (Exception e){
            settlement.setStatus(SettlementStatus.FAILED);
            settlementRepository.save(settlement);
        }

    }

    @Transactional
    public void resolveTransfer(UUID settlementID, String utrNumber, String errorCode, String errorDescription) {

        Settlement settlement = settlementRepository.findById(settlementID).orElseThrow(() -> new ResourceNotFoundException("Settlement",settlementID));

        if(!settlement.getStatus().equals(SettlementStatus.TRANSFER_PENDING)){
            log.info("Settlement resolved, skipping for id:{}", settlement.getId());
            return;
        }

        if(errorCode == null){
            settlement.setStatus(SettlementStatus.PROCESSED);
            settlement.setProcessedAt(LocalDateTime.now());
            settlementRepository.save(settlement);

            List<SettlementPayment> settlementPaymentList = settlementPaymentRepository.findBySettlement(settlement);

            List<UUID> paymentIds = settlementPaymentList.stream().map(SettlementPayment::getId).map(SettlementPaymentId::getPaymentId).toList();
            settlementIntegrationGateway.markSettled(paymentIds);

            outboxEventPublisher.publish(EventAggregateType.SETTLEMENT, settlementID, "SETTLEMENT_PROCESSED", Map.of("settlementId",settlement, "merchantId", settlement.getMerchantId(), "status", settlement.getStatus().name(), "settlementAmount", settlement.getNetAmount().getAmountUnits(), "settlementCurrency", settlement.getNetAmount().getCurrency()));
            // outboxEventPublisher.publish(SETTLEMENT, sttlementId, SETTLEMENT_PROCESSED, Map.of("settlementId", settlement, "merchantId", settlement.getMerchantId(), "status", settlement.getStatus().name(), "settlementAmount", settlement.getNetAmount().getAmountUnits(), "settlementCurrency", settlement.getNetAmount().getCurrency()))
        }
        else{
            settlement.setStatus(SettlementStatus.FAILED);
            settlement.setFailureReason(errorCode + " : "+ errorDescription);
            settlementRepository.save(settlement);
        }
    }
}