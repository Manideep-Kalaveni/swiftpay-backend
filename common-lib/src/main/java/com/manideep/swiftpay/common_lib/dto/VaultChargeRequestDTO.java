package com.manideep.swiftpay.common_lib.dto;

import com.manideep.swiftpay.common_lib.entity.Money;

import java.util.Map;
import java.util.UUID;

public record VaultChargeRequestDTO(
        UUID paymentId,
        String token,
        Money amount,
        Map<String,Object> methodDetails
) {
}
