package com.manideep.swiftpay.merchant_service.mapper;

import com.manideep.swiftpay.merchant_service.dto.response.WebhookConfigResponseDTO;
import com.manideep.swiftpay.merchant_service.entity.MerchantWebhookConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WebhookConfigMapper {
    @Mapping(source = "rawSecret", target = "webhookSecret")
    WebhookConfigResponseDTO toResponseDTO(MerchantWebhookConfig webhookConfig, String rawSecret);
}
