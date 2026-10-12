package com.manideep.swiftpay.merchant_service.controller;

import com.manideep.swiftpay.common_lib.context.MerchantContext;
import com.manideep.swiftpay.merchant_service.dto.request.ApiKeyRequestDTO;
import com.manideep.swiftpay.merchant_service.dto.response.ApiKeyResponseDTO;
import com.manideep.swiftpay.merchant_service.dto.response.ApiKeyResponseListDTO;
import com.manideep.swiftpay.merchant_service.service.ApiKeyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/merchants/api-keys")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final MerchantContext merchantContext;

    @PostMapping
    public ResponseEntity<ApiKeyResponseDTO> createApiKey(@RequestBody @Valid ApiKeyRequestDTO apiKeyRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createApiKey(apiKeyRequestDTO, merchantContext.getMerchantId()));
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponseListDTO>> getApiKeys(){
        return ResponseEntity.ok(apiKeyService.getApiKeysForMerchant(merchantContext.getMerchantId()));
    }

    @DeleteMapping("/{keyId}")
    public ResponseEntity<Void> deactivateKey(@PathVariable String keyId){
        apiKeyService.deactivateApiKey(merchantContext.getMerchantId(),keyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{keyId}/rotate")
    public ResponseEntity<ApiKeyResponseDTO> rotateApiKey(@PathVariable String keyId){
        return ResponseEntity.ok(apiKeyService.rotateApiKey(merchantContext.getMerchantId(), keyId));
    }

}
