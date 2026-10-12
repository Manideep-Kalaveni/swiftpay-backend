package com.manideep.swiftpay.vault_service.controller;

import com.manideep.swiftpay.common_lib.context.MerchantContext;
import com.manideep.swiftpay.vault_service.dto.request.TokenizeRequestDTO;
import com.manideep.swiftpay.vault_service.dto.response.TokenizeResponseDTO;
import com.manideep.swiftpay.vault_service.service.VaultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;
    //final UUID merchantId = UUID.fromString("1313413sdfkljew"); // todo fetch from token
    private final MerchantContext merchantContext;

    @PostMapping("/tokenize")
    public ResponseEntity<TokenizeResponseDTO> tokenize(@Valid @RequestBody TokenizeRequestDTO tokenizeRequestDTO){
        return ResponseEntity.ok(vaultService.tokenize(tokenizeRequestDTO,merchantContext.getMerchantId()));
    }
}
