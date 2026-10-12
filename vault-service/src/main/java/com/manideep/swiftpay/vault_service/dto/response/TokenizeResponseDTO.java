package com.manideep.swiftpay.vault_service.dto.response;

import com.manideep.swiftpay.common_lib.enums.CardBrand;

public record TokenizeResponseDTO(
        String token,
        String lastFour,
        CardBrand cardBrand,
        Integer expiryMonth,
        Integer expiryYear
) {
}
