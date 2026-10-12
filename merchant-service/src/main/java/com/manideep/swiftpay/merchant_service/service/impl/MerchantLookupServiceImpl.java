package com.manideep.swiftpay.merchant_service.service.impl;

import com.manideep.swiftpay.common_lib.dto.SettlementBankDetailsDTO;
import com.manideep.swiftpay.common_lib.dto.WebhookTargetResponseDTO;
import com.manideep.swiftpay.common_lib.enums.MerchantStatus;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.merchant_service.api.MerchantLookupService;
import com.manideep.swiftpay.merchant_service.entity.Merchant;
import com.manideep.swiftpay.merchant_service.repository.MerchantRepository;
import com.manideep.swiftpay.merchant_service.repository.WebhookConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantLookupServiceImpl implements MerchantLookupService {

    private final MerchantRepository merchantRepository;
    private final WebhookConfigRepository webhookConfigRepository;
    private final BytesEncryptor bytesEncryptor;

    @Override
    public List<WebhookTargetResponseDTO> getActiveConfigsForEvent(UUID merchantId, String eventType) {
        return webhookConfigRepository.findByMerchant_IdAndEventTypesAndEnabledTrue(merchantId,eventType).stream()
                .map(config -> {
                    byte[] encryptedSecretBytes = Base64.getDecoder().decode(config.getWebhookSecret());
                    byte[] decryptedSecretBytes = bytesEncryptor.decrypt(encryptedSecretBytes);
                    return new WebhookTargetResponseDTO(
                            config.getId(),
                            config.getTargetUrl(),
                            new String(decryptedSecretBytes, StandardCharsets.UTF_8)
                    );
                })
                .toList();
    }

    @Override
    public List<UUID> getActiveMerchants() {
        return merchantRepository.findByStatus(MerchantStatus.ACTIVE).stream()
                .map(Merchant::getId)
                .toList();
    }

    @Override
    public SettlementBankDetailsDTO getSettlementBankDetails(UUID merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new ResourceNotFoundException("Merchant", merchantId));
        return new SettlementBankDetailsDTO(merchant.getSettlementBankAccount(), merchant.getSettlementBankIfscCode(), merchant.getSettlementBankAccountHolderName());
    }
}
