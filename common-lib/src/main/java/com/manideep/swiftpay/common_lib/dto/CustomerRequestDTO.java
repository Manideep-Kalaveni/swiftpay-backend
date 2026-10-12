package com.manideep.swiftpay.common_lib.dto;

import java.util.UUID;

public record CustomerRequestDTO(
        UUID merchantId,
        String name,
        String email,
        String phone
) {
}
