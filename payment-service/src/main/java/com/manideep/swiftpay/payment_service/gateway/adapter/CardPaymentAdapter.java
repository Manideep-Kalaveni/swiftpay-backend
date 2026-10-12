package com.manideep.swiftpay.payment_service.gateway.adapter;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.common_lib.dto.VaultChargeRequestDTO;
import com.manideep.swiftpay.payment_service.client.VaultServiceClient;
import com.manideep.swiftpay.payment_service.gateway.PaymentAdapter;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentRequest;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentAdapter {

    private final VaultServiceClient vaultServiceClient;

    @Override
    @CircuitBreaker(name = "vault-service")
    @Retry(name = "vault-service")
    public PaymentResult initiate(PaymentRequest request) {

        String token = (String) request.methodDetails().get("token");

        PaymentProcessorResponse paymentProcessorResponse = vaultServiceClient.charge(new VaultChargeRequestDTO(request.paymentId(),token,request.money(),request.methodDetails()));

        return switch(paymentProcessorResponse){
            case PaymentProcessorResponse.Success success -> new PaymentResult.Success(success.bankReference());
            case PaymentProcessorResponse.Failed failed -> new PaymentResult.Failure(failed.errorCode(), failed.errorDescription());
            case PaymentProcessorResponse.Pending pending -> new PaymentResult.Pending(pending.processorReference());
        };
    }

    @Override
    public PaymentResult capture(UUID paymentId) {
        return new PaymentResult.Success("CARD_REF");
    }
}
