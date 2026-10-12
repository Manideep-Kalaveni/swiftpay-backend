package com.manideep.swiftpay.common_lib.dto;

import java.util.UUID;

public record WebhookTargetResponseDTO(
        UUID configId,
        String targetUrl,
        String webhookSecret
) {
}
