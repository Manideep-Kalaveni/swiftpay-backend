package com.manideep.swiftpay.merchant_service.service;


import com.manideep.swiftpay.merchant_service.dto.request.ApiKeyRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.ApiKeyResponseDTO;
import com.manideep.swiftpay.merchant_service.dto.response.ApiKeyResponseListDTO;

import java.util.List;
import java.util.UUID;

public interface ApiKeyService {
    ApiKeyResponseDTO createApiKey(ApiKeyRequestDTO apiKeyRequestDTO, UUID id);

    List<ApiKeyResponseListDTO> getApiKeysForMerchant(UUID merchantId);

    void deactivateApiKey(UUID merchantId, String keyId);

    ApiKeyResponseDTO rotateApiKey(UUID merchantId, String keyId);
}
