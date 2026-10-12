package com.manideep.swiftpay.merchant_service.controller;

import com.manideep.swiftpay.common_lib.cache.ApiKeyCacheEntry;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.merchant_service.entity.ApiKey;
import com.manideep.swiftpay.merchant_service.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/api-keys")
@RequiredArgsConstructor
public class InternalApiKeyController {

    private final ApiKeyRepository apiKeyRepository;

    @GetMapping("/{keyId}")
    public ApiKeyCacheEntry findByKeyId(@PathVariable String keyId){
        ApiKey apiKey = apiKeyRepository.findByKeyId(keyId).orElseThrow(() -> new ResourceNotFoundException("API-KEY",keyId));

        return new ApiKeyCacheEntry(apiKey.getKeyId(), apiKey.getKeySecretHash(), apiKey.getPreviousKeySecretHash(), apiKey.getGracePeriodEndAt(), apiKey.getMerchant().getId(), apiKey.getEnvironment(), apiKey.isEnabled());
    }
}
