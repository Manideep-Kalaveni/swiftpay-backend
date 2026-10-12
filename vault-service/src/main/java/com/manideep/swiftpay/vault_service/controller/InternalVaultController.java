package com.manideep.swiftpay.vault_service.controller;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.common_lib.dto.VaultChargeRequestDTO;
import com.manideep.swiftpay.vault_service.service.VaultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/vault")
@RequiredArgsConstructor
public class InternalVaultController {

    private final VaultService vaultService;

    @PostMapping("/client")
    public PaymentProcessorResponse charge(@RequestBody VaultChargeRequestDTO vaultChargeRequestDTO){
        return vaultService.charge(vaultChargeRequestDTO.paymentId(),vaultChargeRequestDTO.token(),vaultChargeRequestDTO.amount(),vaultChargeRequestDTO.methodDetails());
    }
}
