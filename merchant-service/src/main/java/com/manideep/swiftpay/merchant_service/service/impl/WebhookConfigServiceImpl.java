package com.manideep.swiftpay.merchant_service.service.impl;

import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.common_lib.util.RandomizerUtil;
import com.manideep.swiftpay.merchant_service.dto.request.WebhookConfigRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.WebhookConfigResponseDTO;
import com.manideep.swiftpay.merchant_service.entity.Merchant;
import com.manideep.swiftpay.merchant_service.entity.MerchantWebhookConfig;
import com.manideep.swiftpay.merchant_service.mapper.WebhookConfigMapper;
import com.manideep.swiftpay.merchant_service.repository.MerchantRepository;
import com.manideep.swiftpay.merchant_service.repository.WebhookConfigRepository;
import com.manideep.swiftpay.merchant_service.service.WebhookConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookConfigServiceImpl implements WebhookConfigService {

    private final WebhookConfigRepository webhookConfigRepository;
    private final MerchantRepository merchantRepository;
    private final BytesEncryptor bytesEncryptor;
    private final WebhookConfigMapper webhookConfigMapper;

    @Override
    public WebhookConfigResponseDTO createMerchantWebhook(UUID merchantId, WebhookConfigRequestDTO webhookConfigRequestDTO) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));

        String rawSecret = RandomizerUtil.randomBase64(32);
        byte[] rawSecretBytes = rawSecret.getBytes(StandardCharsets.UTF_8);

        String encryptedSecret = Base64.getEncoder().encodeToString(bytesEncryptor.encrypt(rawSecretBytes));

        MerchantWebhookConfig webhookConfig = MerchantWebhookConfig.builder()
                .merchant(merchant)
                .targetUrl(webhookConfigRequestDTO.targetUrl())
                .eventTypes(webhookConfigRequestDTO.eventTypes())
                .webhookSecret(encryptedSecret)
                .build();


        return webhookConfigMapper.toResponseDTO(webhookConfig,rawSecret);
    }

    @Override
    public List<WebhookConfigResponseDTO> getMerchantWebhooks(UUID merchantId, UUID webhookConfigId) {
        return webhookConfigRepository.findByMerchant_Id(merchantId).stream()
                .map(config -> webhookConfigMapper.toResponseDTO(config, null))
                .toList();
    }

    @Override
    @Transactional
    public WebhookConfigResponseDTO updateMerchantWebhook(UUID merchantId, UUID webhookConfigId, WebhookConfigRequestDTO requestDTO) {
        MerchantWebhookConfig webhookConfig = webhookConfigRepository.findByMerchant_IdAndEnabledTrue(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant_Webhook_Config" , merchantId));

        webhookConfig.setTargetUrl(requestDTO.targetUrl());
        webhookConfig.setEventTypes(requestDTO.eventTypes());

        MerchantWebhookConfig saved = webhookConfigRepository.save(webhookConfig);
        log.info("Updated webhook config {} for merchant {}", saved.getId(), merchantId);

        return webhookConfigMapper.toResponseDTO(saved, null);
    }

    @Override
    public WebhookConfigResponseDTO getByMerchant(UUID merchantId, UUID webhookConfigId) {
        MerchantWebhookConfig webhookConfig = configByMerchant(merchantId,webhookConfigId);
        return webhookConfigMapper.toResponseDTO(webhookConfig, null);
    }

    @Override
    public void deleteMerchantWebhook(UUID merchantId, UUID webhookConfigId) {
        MerchantWebhookConfig webhookConfig = configByMerchant(merchantId,webhookConfigId);
        webhookConfigRepository.delete(webhookConfig);
        log.info("Deleted webhook config {} for merchant {}", webhookConfigId, merchantId);
    }



    private MerchantWebhookConfig configByMerchant(UUID merchantId, UUID webhookConfigId){
        return webhookConfigRepository.findByIdAndMerchant_Id(webhookConfigId,merchantId).orElseThrow(() -> new ResourceNotFoundException("Merchant_Webhook_Config",webhookConfigId));
    }
}

