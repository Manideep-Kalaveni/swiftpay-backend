package com.manideep.swiftpay.payment_service.statemachine;

import com.manideep.swiftpay.common_lib.context.MerchantContext;
import com.manideep.swiftpay.common_lib.enums.PaymentActor;
import com.manideep.swiftpay.common_lib.enums.PaymentEvent;
import com.manideep.swiftpay.common_lib.enums.PaymentStatus;
import com.manideep.swiftpay.payment_service.entity.Payment;
import com.manideep.swiftpay.payment_service.entity.PaymentTransitionLog;
import com.manideep.swiftpay.payment_service.repository.PaymentTransitionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentTransitionLogService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;
    private final MerchantContext merchantContext;


    public PaymentStatus apply(Payment payment, PaymentEvent event){
        PaymentStatus next = paymentStateMachine.transition(payment.getPaymentStatus(), event);

        PaymentTransitionLog paymentTransitionLog = PaymentTransitionLog.builder()
                .payment(payment)
                .fromStatus(payment.getPaymentStatus())
                .paymentEvent(event)
                .toStatus(next)
                .actor(getActor())
                .occurredAt(LocalDateTime.now())
                .build();

        paymentTransitionLogRepository.save(paymentTransitionLog);

        payment.setPaymentStatus(next);
        return next;
    }

    private PaymentActor getActor(){
        try{
            String keyId = merchantContext.getKeyId();
            UUID merchantId = merchantContext.getMerchantId();

            if(keyId != null && !keyId.isBlank()){
                return PaymentActor.CUSTOMER;
            }
            else if(merchantId != null){
                return PaymentActor.MERCHANT;
            }
        } catch (Exception e) {

        }
        return PaymentActor.SYSTEM;
    }
}
