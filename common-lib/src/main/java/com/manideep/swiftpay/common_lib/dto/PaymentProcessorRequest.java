package com.manideep.swiftpay.common_lib.dto;

import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.common_lib.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentProcessorRequest(
        UUID paymentId,
        UUID processingId,
        PaymentMethod method,
        Money money,
        String pan,
        String expiry,
        Map<String,Object> methodDetails
) {

    public static PaymentProcessorRequest card(UUID paymentId, String pan, String expiry, Map<String,Object> methodDetails, Money money){
        return new PaymentProcessorRequest(paymentId,UUID.randomUUID(),PaymentMethod.CARD,money,pan,expiry,methodDetails);
    }

    public static PaymentProcessorRequest nonCard(UUID paymentId, PaymentMethod method, Money money, Map<String,Object> methodDetails){
        return new PaymentProcessorRequest(paymentId,UUID.randomUUID(),method,money,null,null,methodDetails);
    }
}
