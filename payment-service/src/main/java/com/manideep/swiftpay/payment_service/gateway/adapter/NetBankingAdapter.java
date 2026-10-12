package com.manideep.swiftpay.payment_service.gateway.adapter;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorRequest;
import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.common_lib.enums.PaymentMethod;
import com.manideep.swiftpay.payment_service.gateway.PaymentAdapter;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentRequest;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentResult;
import com.manideep.swiftpay.payment_service.processor.PaymentProcessorRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class NetBankingAdapter implements PaymentAdapter {

    private final PaymentProcessorRouter paymentProcessorRouter;

    @Override
    public PaymentResult initiate(PaymentRequest request) {
        log.info("Initiating Net Banking Payment Request with payment id : {}", request.paymentId());

        try{

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.nonCard(
                    request.paymentId(), PaymentMethod.NET_BANKING,
                    request.money(), request.methodDetails()
            );

            PaymentProcessorResponse paymentProcessorResponse = paymentProcessorRouter.charge(paymentProcessorRequest);


            return switch(paymentProcessorResponse){
                case PaymentProcessorResponse.Failed failed ->
                        new PaymentResult.Failure(failed.errorCode(), failed.errorDescription());
                case PaymentProcessorResponse.Pending pending ->
                        new PaymentResult.Pending(pending.processorReference());
                case PaymentProcessorResponse.Success success ->
                        new PaymentResult.Success(success.bankReference());
            };
        } catch (Exception e) {
            log.warn("Payment with id : {} failed", request.paymentId());
            return new PaymentResult.Failure("NET_BANKING_FAILED","Net Banking payment failed");
        }

    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("BANK_REF");
    }
}
