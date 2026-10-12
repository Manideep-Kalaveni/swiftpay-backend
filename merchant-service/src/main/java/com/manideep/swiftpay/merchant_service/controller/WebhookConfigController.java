package com.manideep.swiftpay.merchant_service.controller;

import com.manideep.swiftpay.common_lib.context.MerchantContext;
import com.manideep.swiftpay.merchant_service.dto.request.WebhookConfigRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.WebhookConfigResponseDTO;
import com.manideep.swiftpay.merchant_service.service.WebhookConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/merchants/webhooks")
@RequiredArgsConstructor
public class WebhookConfigController {

    private final WebhookConfigService webhookConfigService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<WebhookConfigResponseDTO> createMerchantWebhook(@RequestBody @Valid WebhookConfigRequestDTO webhookConfigRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(webhookConfigService.createMerchantWebhook(merchantContext.getMerchantId(), webhookConfigRequestDTO));
    }

    @GetMapping
    public ResponseEntity<List<WebhookConfigResponseDTO>> getMerchantWebhooks(@RequestParam(required = false) UUID configId){
        return ResponseEntity.ok(webhookConfigService.getMerchantWebhooks(merchantContext.getMerchantId(), configId));
    }

    @GetMapping("/{webhookConfigId}")
    public ResponseEntity<WebhookConfigResponseDTO> getByMerchant(@PathVariable UUID webhookConfigId){
        return ResponseEntity.ok(webhookConfigService.getByMerchant(merchantContext.getMerchantId(), webhookConfigId));
    }

    @PutMapping("/{webhookConfigId}")
    public ResponseEntity<WebhookConfigResponseDTO> updateMerchantWebhook(@PathVariable UUID webhookConfigId,
                                                                          @RequestBody @Valid WebhookConfigRequestDTO webhookConfigRequestDTO){
        return ResponseEntity.ok(webhookConfigService.updateMerchantWebhook(merchantContext.getMerchantId(), webhookConfigId, webhookConfigRequestDTO));
    }

    @DeleteMapping("/{webhookConfigId}")
    public ResponseEntity<Void> deleteMerchantWebhook(@PathVariable UUID webhookConfigId){
        webhookConfigService.deleteMerchantWebhook(merchantContext.getMerchantId(), webhookConfigId);
        return ResponseEntity.noContent().build();
    }
}