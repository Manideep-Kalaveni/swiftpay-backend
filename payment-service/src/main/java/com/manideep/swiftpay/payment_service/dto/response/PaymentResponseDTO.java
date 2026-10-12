package com.manideep.swiftpay.payment_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.common_lib.enums.PaymentMethod;
import com.manideep.swiftpay.common_lib.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PaymentResponseDTO(
        UUID id,
        UUID orderId,
        UUID merchantId,
        PaymentStatus paymentStatus,
        String idempotencyKey,
        Money money,
        PaymentMethod paymentMethod,
        Map<String, Object> paymentDetails,
        String bankReference,
        String errorCode,
        String errorDescription,
        LocalDateTime authorizedAt,
        LocalDateTime capturedAt,
        LocalDateTime failedAt,
        LocalDateTime refundedAt,
        LocalDateTime settledAt
) {
}
