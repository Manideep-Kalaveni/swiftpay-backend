package com.manideep.swiftpay.payment_service.processor.strategy;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorRequest;
import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.payment_service.processor.PaymentProcessor;

public class CardPaymentProcessor implements PaymentProcessor {
    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        return null;
    }
}
