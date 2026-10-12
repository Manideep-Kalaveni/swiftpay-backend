package com.manideep.swiftpay.merchant_service.service.impl;

import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.common_lib.util.RandomizerUtil;
import com.manideep.swiftpay.merchant_service.dto.request.ApiKeyRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.ApiKeyResponseDTO;
import com.manideep.swiftpay.merchant_service.dto.response.ApiKeyResponseListDTO;
import com.manideep.swiftpay.merchant_service.entity.ApiKey;
import com.manideep.swiftpay.merchant_service.entity.Merchant;
import com.manideep.swiftpay.merchant_service.mapper.ApiKeyMapper;
import com.manideep.swiftpay.merchant_service.repository.ApiKeyRepository;
import com.manideep.swiftpay.merchant_service.repository.MerchantRepository;
import com.manideep.swiftpay.merchant_service.service.ApiKeyService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;
    private final ApiKeyMapper apiKeyMapper;

    @Override
    @Transactional
    public ApiKeyResponseDTO createApiKey(ApiKeyRequestDTO apiKeyRequestDTO, UUID merchantId) {

        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new ResourceNotFoundException("merchant", merchantId));

        String keyId = "spay_" + apiKeyRequestDTO.environment().toString().toLowerCase() + "_" + RandomizerUtil.randomBase64(24);
        String keySecret = RandomizerUtil.randomBase64(40);
        ApiKey apiKey = ApiKey.builder()
                .environment(apiKeyRequestDTO.environment())
                .merchant(merchant)
                .keyId(keyId)
                .keySecretHash(keySecret)
                .build();

        apiKey = apiKeyRepository.save(apiKey);

        return apiKeyMapper.toApiKeyResponseDTO(apiKey);
    }

    @Override
    public List<ApiKeyResponseListDTO> getApiKeysForMerchant(UUID merchantId) {
        return apiKeyRepository.findByMerchant_IdAndEnabledTrue(merchantId).stream()
                .map(apiKeyMapper::toApiKeyListResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public void deactivateApiKey(UUID merchantId, String keyId) {
        ApiKey apiKey = getApiKeyWithMerchantId(merchantId,keyId);

        apiKey.setEnabled(false);
        apiKeyRepository.save(apiKey);
    }

    @Override
    public ApiKeyResponseDTO rotateApiKey(UUID merchantId, String keyId) {

        ApiKey apiKey = getApiKeyWithMerchantId(merchantId, keyId);

        String newApiSecret = RandomizerUtil.randomBase64(40);

        apiKey.setPreviousKeySecretHash(apiKey.getKeySecretHash());
        apiKey.setKeySecretHash(newApiSecret);
        apiKey.setRotatedAt(LocalDateTime.now());
        apiKey.setGracePeriodEndAt(LocalDateTime.now().plusHours(24));

        apiKey = apiKeyRepository.save(apiKey);
        return apiKeyMapper.toApiKeyResponseDTO(apiKey);
    }


    private ApiKey getApiKeyWithMerchantId(UUID merchantId, String keyId){
        return apiKeyRepository.findByKeyId(keyId)
                .filter(k -> k.getMerchant().getId().equals(merchantId))
                .orElseThrow(() -> new ResourceNotFoundException("ApiKey", keyId));
    }
}
