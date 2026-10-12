package com.manideep.swiftpay.payment_service.dto.request;

import com.manideep.swiftpay.common_lib.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PaymentRequestDTO(
        @NotNull(message = "Order id is Required")
        UUID orderId,
        @NotNull(message = "Payment method is Required")
        PaymentMethod method,
        Map<String, Object> methodDetails
) {


}
