package com.manideep.swiftpay.payment_service.service.impl;

import com.manideep.swiftpay.common_lib.enums.EventAggregateType;
import com.manideep.swiftpay.common_lib.enums.OrderStatus;
import com.manideep.swiftpay.common_lib.enums.PaymentEvent;
import com.manideep.swiftpay.common_lib.enums.PaymentStatus;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.payment_service.dto.request.PaymentRequestDTO;
import com.manideep.swiftpay.payment_service.dto.response.PaymentResponseDTO;
import com.manideep.swiftpay.payment_service.entity.Order;
import com.manideep.swiftpay.payment_service.entity.Payment;
import com.manideep.swiftpay.payment_service.gateway.PaymentGatewayRouter;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentRequest;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentResult;
import com.manideep.swiftpay.payment_service.mapper.PaymentMapper;
import com.manideep.swiftpay.payment_service.outbox.OutboxEventPublisher;
import com.manideep.swiftpay.payment_service.repository.OrderRepository;
import com.manideep.swiftpay.payment_service.repository.PaymentRepository;
import com.manideep.swiftpay.payment_service.saga.PaymentAuthorizationRecorder;
import com.manideep.swiftpay.payment_service.service.PaymentService;
import com.manideep.swiftpay.payment_service.statemachine.PaymentTransitionLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final PaymentTransitionLogService paymentTransitionLogService;
    private final OutboxEventPublisher outboxEventPublisher;
    private final PaymentAuthorizationRecorder paymentAuthorizationRecorder;

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PaymentResponseDTO createPayment(UUID merchantId, PaymentRequestDTO paymentRequestDTO, String idempotencyKey) {

        if(idempotencyKey != null){
            var existingPayment = paymentAuthorizationRecorder.findExistingAttempt(merchantId,idempotencyKey);
            if(existingPayment.isPresent()){
                return existingPayment.get();
            }
        }

        Payment payment = paymentAuthorizationRecorder.record(merchantId,paymentRequestDTO,idempotencyKey);

        PaymentRequest paymentRequest = new PaymentRequest(payment.getId(),merchantId, paymentRequestDTO.orderId(), payment.getMoney(), payment.getPaymentMethod(),payment.getPaymentDetails());

        PaymentResult result = null;
        try{
            result = paymentGatewayRouter.initiate(paymentRequest);
        }
        catch (Exception e){
            paymentAuthorizationRecorder.compensateAuthorizationFailed(payment.getId(), "PAYMENT_GATEWAY_UNREACHABLE", e.getMessage());
        }

        return paymentAuthorizationRecorder.applyGatewayResult(payment.getId(),result);
    }

    @Override
    public PaymentResponseDTO capture(UUID merchantId, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndMerchantId(paymentId,merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT",paymentId));


        //payment.setPaymentStatus(PaymentStatus.CAPTURING);

        paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getPaymentMethod(), paymentId);

        if(paymentResult instanceof PaymentResult.Success success){
            //payment.setPaymentStatus(PaymentStatus.CAPTURED);
            paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(LocalDateTime.now());
        }
        else if(paymentResult instanceof PaymentResult.Failure failure){
            payment.setErrorDescription(failure.errorDescription());
            payment.setErrorCode(failure.errorCode());
            //payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_FAIL);
        }

        outboxEventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_STATUS_CHANGED",
                Map.of(
                        "paymentId", payment.getId(),
                        "merchantId", payment.getMerchantId(),
                        "paymentStatus", payment.getPaymentStatus().name(),
                        "amountUnits", payment.getMoney().getAmountUnits(),
                        "amountCurrency", payment.getMoney().getCurrency()
                )
        );
        return paymentMapper.toPaymentResponseDTO(payment);
    }

    @Override
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String simBankErrorCode, String simulatorBankDeclined) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        if(!payment.getPaymentStatus().equals(PaymentStatus.AUTHORIZING)){
            log.warn("Payment is not in Authorizing State, paymentId : {}, status : {}", paymentId, payment.getPaymentStatus());
            return;
        }

        Order order = payment.getOrder();

        if(approve){
            paymentTransitionLogService.apply(payment,PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(LocalDateTime.now());

            // AUTO-CAPTURE
            paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            PaymentResult captureResult = paymentGatewayRouter.capture(payment.getPaymentMethod(), paymentId);

            if(captureResult instanceof PaymentResult.Success success){
                paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(LocalDateTime.now());
                order.setOrderStatus(OrderStatus.PAID);
            }
            else if(captureResult instanceof PaymentResult.Failure failure){
                paymentTransitionLogService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        }
        else{
            paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(simBankErrorCode);
            payment.setErrorDescription(simulatorBankDeclined);
        }

        paymentRepository.save(payment);
        orderRepository.save(order);


    }
}
