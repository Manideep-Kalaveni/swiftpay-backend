package com.manideep.swiftpay.merchant_service.mapper;

import com.manideep.swiftpay.merchant_service.dto.response.ApiKeyResponseDTO;
import com.manideep.swiftpay.merchant_service.dto.response.ApiKeyResponseListDTO;
import com.manideep.swiftpay.merchant_service.entity.ApiKey;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApiKeyMapper {
    ApiKeyResponseDTO toApiKeyResponseDTO(ApiKey apiKey);

    ApiKeyResponseListDTO toApiKeyListResponseDTO(ApiKey key);
}
