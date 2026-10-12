package com.manideep.swiftpay.payment_service.processor;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorRequest;
import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;


public interface PaymentProcessor {

    PaymentProcessorResponse charge(PaymentProcessorRequest request);
}
