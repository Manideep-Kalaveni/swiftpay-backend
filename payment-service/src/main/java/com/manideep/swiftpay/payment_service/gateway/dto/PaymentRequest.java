package com.manideep.swiftpay.payment_service.gateway.dto;

import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.common_lib.enums.PaymentMethod;

import java.util.Map;
import java.util.UUID;

public record PaymentRequest(
        UUID paymentId,
        UUID merchantId,
        UUID orderId,
        Money money,
        PaymentMethod method,
        Map<String, Object> methodDetails
) {
}
