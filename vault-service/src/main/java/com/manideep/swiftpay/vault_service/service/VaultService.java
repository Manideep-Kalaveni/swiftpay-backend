package com.manideep.swiftpay.vault_service.service;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.vault_service.dto.request.TokenizeRequestDTO;
import com.manideep.swiftpay.vault_service.dto.response.TokenizeResponseDTO;

import java.util.Map;
import java.util.UUID;

public interface VaultService {
    TokenizeResponseDTO tokenize(TokenizeRequestDTO tokenizeRequestDTO, UUID merchantId);

    PaymentProcessorResponse charge(UUID paymentId, String token, Money money, Map<String, Object> paymentMethodDetails);
}
