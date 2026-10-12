package com.manideep.swiftpay.merchant_service.dto.response;

import com.manideep.swiftpay.common_lib.enums.Environment;

import java.util.UUID;

public record ApiKeyResponseDTO(
        UUID id,
        String keyId,
        String keySecretHash,
        Environment environment
) {
}
