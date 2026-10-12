package com.manideep.swiftpay.merchant_service.dto.request;

public record WebhookConfigRequestDTO(
        String targetUrl,
        String eventTypes
) {
}
