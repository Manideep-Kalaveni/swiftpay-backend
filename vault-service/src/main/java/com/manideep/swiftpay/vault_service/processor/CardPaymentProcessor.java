package com.manideep.swiftpay.vault_service.processor;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorRequest;
import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.common_lib.util.RandomizerUtil;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class CardPaymentProcessor{

    public static final String PAN_CARD_DECLINED = "400000000002";
    public static final String PAN_CARD_EXPIRED = "400000000007";

    @Bulkhead(name = "vault-card-processor", type = Bulkhead.Type.THREADPOOL)
    public CompletableFuture<PaymentProcessorResponse> charge(PaymentProcessorRequest request) {
        String pan = request.pan();

        if(PAN_CARD_DECLINED.equals(pan)){
            log.warn("Card Declined");
            return CompletableFuture.completedFuture(new PaymentProcessorResponse.Failed("CARD_DECLINED", "Card Declined by Bank"));
        }

        if(PAN_CARD_EXPIRED.equals(pan)){
            log.warn("Card Expired");
            return CompletableFuture.completedFuture(new PaymentProcessorResponse.Failed("CARD_EXPIRED", "Card Expired by Bank"));
        }

        String processorRef = "CARD_PROCESSOR_"+ RandomizerUtil.randomBase64(16);
        return CompletableFuture.completedFuture(new PaymentProcessorResponse.Pending(processorRef));
    }
}