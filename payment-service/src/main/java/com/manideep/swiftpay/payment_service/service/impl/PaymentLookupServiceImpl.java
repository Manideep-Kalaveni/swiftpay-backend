package com.manideep.swiftpay.payment_service.service.impl;

import com.manideep.swiftpay.common_lib.dto.PaymentSettlementView;
import com.manideep.swiftpay.common_lib.enums.PaymentStatus;
import com.manideep.swiftpay.payment_service.api.PaymentLookupService;
import com.manideep.swiftpay.payment_service.entity.Payment;
import com.manideep.swiftpay.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookupServiceImpl implements PaymentLookupService {

    private final PaymentRepository paymentRepository;

    @Override
    public List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId) {
        return paymentRepository.findByMerchantIdAndPaymentStatusForUpdate(merchantId, PaymentStatus.CAPTURED).stream()
                .map(payment -> {
                    return new PaymentSettlementView(payment.getId(),payment.getMoney().getAmountUnits(),0,payment.getMoney().getCurrency());
                }).toList();
    }

    @Override
    @Transactional
    public void markSettled(List<UUID> paymentIds) {
        List<Payment> payments = paymentRepository.findAllById(paymentIds);

        for(Payment payment : payments){
            payment.setPaymentStatus(PaymentStatus.SETTLED);
            payment.setSettledAt(LocalDateTime.now());
        }

        paymentRepository.saveAll(payments);
    }
}
