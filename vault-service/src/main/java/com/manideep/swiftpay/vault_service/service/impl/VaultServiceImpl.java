package com.manideep.swiftpay.vault_service.service.impl;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorRequest;
import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.common_lib.enums.CardBrand;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.common_lib.util.RandomizerUtil;
import com.manideep.swiftpay.vault_service.config.VaultEncryptConfig;
import com.manideep.swiftpay.vault_service.dto.request.TokenizeRequestDTO;
import com.manideep.swiftpay.vault_service.dto.response.TokenizeResponseDTO;
import com.manideep.swiftpay.vault_service.entity.CardToken;
import com.manideep.swiftpay.vault_service.entity.VaultCard;
import com.manideep.swiftpay.vault_service.mapper.VaultMapper;
import com.manideep.swiftpay.vault_service.processor.CardPaymentProcessor;
import com.manideep.swiftpay.vault_service.repository.CardTokenRepository;
import com.manideep.swiftpay.vault_service.repository.VaultCardRepository;
import com.manideep.swiftpay.vault_service.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VaultServiceImpl implements VaultService {

    private final VaultCardRepository vaultCardRepository;
    private final CardTokenRepository cardTokenRepository;
    private final BytesEncryptor dekEncryptor;
    private final VaultMapper vaultMapper;
    private final CardPaymentProcessor cardPaymentProcessor;

    @Override
    public TokenizeResponseDTO tokenize(TokenizeRequestDTO tokenizeRequestDTO, UUID merchantId) {

        CardBrand cardBrand = detectBrand(tokenizeRequestDTO.pan());
        String lastFour = tokenizeRequestDTO.pan().substring(tokenizeRequestDTO.pan().length()-4);
        String bin = tokenizeRequestDTO.pan().substring(0,6);


        byte[] dek = KeyGenerators.secureRandom(32).generateKey();
        byte[] encryptedPan = VaultEncryptConfig.panEncryptor(dek).encrypt(tokenizeRequestDTO.pan().getBytes(StandardCharsets.UTF_8));
        byte[] encryptedDek = dekEncryptor.encrypt(dek);

        VaultCard vaultCard = VaultCard.builder()
                .bin(bin)
                .lastFour(lastFour)
                .cardBrand(cardBrand)
                .expiryMonth(tokenizeRequestDTO.expiryMonth().toString())
                .expiryYear(tokenizeRequestDTO.expiryYear().toString())
                .encryptedPan(encryptedPan)
                .encryptedDek(encryptedDek)
                .build();

        vaultCardRepository.save(vaultCard);


        String token = "token" + RandomizerUtil.randomBase64(32);

        CardToken cardToken = CardToken.builder()
                .token(token)
                .vaultCard(vaultCard)
                .merchantId(merchantId)
                .customerId(tokenizeRequestDTO.customerId())
                .build();

        cardTokenRepository.save(cardToken);
        return new TokenizeResponseDTO(cardToken.getToken(), vaultCard.getLastFour(), vaultCard.getCardBrand(), Integer.parseInt(vaultCard.getExpiryMonth()),Integer.parseInt(vaultCard.getExpiryYear()));
    }

    @Override
    public PaymentProcessorResponse charge(UUID paymentId, String token, Money money, Map<String, Object> paymentMethodDetails) {
        CardToken cardToken = cardTokenRepository.findByTokenAndRevokedAtIsNull(token)
                .orElseThrow(() -> new ResourceNotFoundException("Card Token", token));

        VaultCard vaultCard = cardToken.getVaultCard();

        byte[] panBytes = null;

        try{
            byte[] dek = dekEncryptor.decrypt(vaultCard.getEncryptedDek());

            panBytes = VaultEncryptConfig.panEncryptor(dek).decrypt(vaultCard.getEncryptedPan());

            String pan = new String(panBytes, StandardCharsets.UTF_8);
            String expiry = vaultCard.getExpiryMonth() + "/" + vaultCard.getExpiryYear();

            PaymentProcessorRequest paymentProcessorRequest = PaymentProcessorRequest.card(paymentId,pan,expiry,paymentMethodDetails,money);
            PaymentProcessorResponse paymentProcessorResponse = cardPaymentProcessor.charge(paymentProcessorRequest).get(5, TimeUnit.SECONDS);
            log.info("Vault Charge Registered , token={}****", token.substring(0,4));
            Arrays.fill(panBytes, (byte) 0);

            return paymentProcessorResponse;
        } catch (Exception e) {
            return new PaymentProcessorResponse.Failed("VAULT_CHARGE_FAILED", e.getMessage());
        }

    }

    private CardBrand detectBrand(String pan) {
        String digits = pan.replaceAll("\\s+", "");

        if (digits.startsWith("4")) {
            return CardBrand.VISA;
        }
        if (digits.matches("^5[1-5]\\d*") || digits.matches("^2(2[2-9]|[3-6]\\d|7[01]|720)\\d*")) {
            return CardBrand.MASTER_CARD;
        }
        if (digits.startsWith("34") || digits.startsWith("37")) {
            return CardBrand.AMEX;
        }
        return CardBrand.RUPAY;
    }
}
