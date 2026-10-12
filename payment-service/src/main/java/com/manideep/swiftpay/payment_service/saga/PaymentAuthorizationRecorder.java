package com.manideep.swiftpay.payment_service.saga;

import com.manideep.swiftpay.common_lib.enums.EventAggregateType;
import com.manideep.swiftpay.common_lib.enums.OrderStatus;
import com.manideep.swiftpay.common_lib.enums.PaymentEvent;
import com.manideep.swiftpay.common_lib.exception.BusinessRuleViolationException;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.payment_service.dto.request.PaymentRequestDTO;
import com.manideep.swiftpay.payment_service.dto.response.PaymentResponseDTO;
import com.manideep.swiftpay.payment_service.entity.Order;
import com.manideep.swiftpay.payment_service.entity.Payment;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentResult;
import com.manideep.swiftpay.payment_service.mapper.PaymentMapper;
import com.manideep.swiftpay.payment_service.outbox.OutboxEventPublisher;
import com.manideep.swiftpay.payment_service.repository.OrderRepository;
import com.manideep.swiftpay.payment_service.repository.PaymentRepository;
import com.manideep.swiftpay.payment_service.statemachine.PaymentTransitionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentAuthorizationRecorder {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransitionLogService paymentTransitionLogService;
    private final PaymentMapper paymentMapper;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public Payment record(UUID merchantId, PaymentRequestDTO paymentRequestDTO, String idempotencyKey){
        Order order = orderRepository.findByIdAndMerchantId(paymentRequestDTO.orderId(), merchantId).orElseThrow(() -> new ResourceNotFoundException("ORDER",paymentRequestDTO.orderId()));


        // TODO add validation check for order status

        if(order.getOrderStatus() != OrderStatus.CREATED && order.getOrderStatus() != OrderStatus.ATTEMPTED){
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE", "Order cannot accept payment in status: "+ order.getOrderStatus());
        }

        order.setOrderStatus(OrderStatus.ATTEMPTED);
        order.setAttempts(order.getAttempts()+1);

        Payment payment = Payment.builder()
                .merchantId(merchantId)
                .idempotencyKey(idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString())
                .paymentMethod(paymentRequestDTO.method())
                .order(order)
                .paymentDetails(paymentRequestDTO.methodDetails())
                .money(order.getMoney())
                .build();

        payment = paymentRepository.save(payment);
        paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        return payment;
    }

    @Transactional
    public PaymentResponseDTO compensateAuthorizationFailed(UUID paymentId, String errorCode, String errorDescription){
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));

        paymentTransitionLogService.apply(payment,PaymentEvent.AUTHORIZE_FAIL);

        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDescription);

        payment = paymentRepository.save(payment);

        // Send compensate event
        publishStatusEvent(payment, "PAYMENT_AUTHORIZATION_COMPENSATED");
        return paymentMapper.toPaymentResponseDTO(payment);
    }

    @Transactional
    public PaymentResponseDTO applyGatewayResult(UUID paymentId, PaymentResult result){
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
        switch (result) {
            case PaymentResult.Pending pending -> payment.setProcessorReference(pending.registrationReference());
            case PaymentResult.Failure failure -> {
                //payment.setPaymentStatus(PaymentStatus.FAILED);
                paymentTransitionLogService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success -> payment.setBankReference(success.bankReference());
        }

        payment = paymentRepository.save(payment);
        publishStatusEvent(payment, "PAYMENT_CREATED");
        return paymentMapper.toPaymentResponseDTO(payment);
    }

    private void publishStatusEvent(Payment payment, String eventType){
        outboxEventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), eventType,
                Map.of("orderId", payment.getOrder().getId().toString(),
                        "paymentId", payment.getId().toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getPaymentStatus().name(),
                        "amountUnits", payment.getMoney().getAmountUnits(),
                        "amountCurrency", payment.getMoney().getCurrency(),
                        "paymentMethod", payment.getPaymentMethod().name()));
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponseDTO> findExistingAttempt(UUID merchantId, String idempotencyKey) {
        return paymentRepository.findByMerchantIdAndIdempotencyKey(merchantId,idempotencyKey).map(paymentMapper::toPaymentResponseDTO);
    }
}
