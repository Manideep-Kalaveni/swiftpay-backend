package com.manideep.swiftpay.merchant_service.service;

import com.manideep.swiftpay.merchant_service.dto.request.WebhookConfigRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.WebhookConfigResponseDTO;

import java.util.List;
import java.util.UUID;

public interface WebhookConfigService {
    WebhookConfigResponseDTO createMerchantWebhook(UUID merchantId, WebhookConfigRequestDTO webhookConfigRequestDTO);

    List<WebhookConfigResponseDTO> getMerchantWebhooks(UUID merchantId, UUID configID);

    WebhookConfigResponseDTO updateMerchantWebhook(UUID merchantId, UUID webhookConfigId, WebhookConfigRequestDTO webhookConfigRequestDTO);

    WebhookConfigResponseDTO getByMerchant(UUID merchantId, UUID webhookConfigId);

    void deleteMerchantWebhook(UUID merchantId, UUID webhookConfigId);
}
