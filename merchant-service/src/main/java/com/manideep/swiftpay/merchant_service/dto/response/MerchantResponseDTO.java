package com.manideep.swiftpay.merchant_service.dto.response;

import com.manideep.swiftpay.common_lib.enums.BusinessType;
import com.manideep.swiftpay.common_lib.enums.MerchantStatus;

import java.util.UUID;

public record MerchantResponseDTO(
        UUID id,
        String name,
        String email,
        BusinessType businessType,
        MerchantStatus merchantStatus,
        String businessName
) {
}
