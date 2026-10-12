package com.manideep.swiftpay.merchant_service.dto.response;

import com.manideep.swiftpay.common_lib.enums.Environment;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApiKeyResponseListDTO(
        UUID id,
        String keyId,
        Environment environment,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt,
        boolean enabled
) {
}
