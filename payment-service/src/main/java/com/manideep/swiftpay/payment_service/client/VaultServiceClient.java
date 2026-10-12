package com.manideep.swiftpay.payment_service.client;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.common_lib.dto.VaultChargeRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "vault-service", path = "/internal/vault")
public interface VaultServiceClient {

    @PostMapping("/client")
    PaymentProcessorResponse charge(@RequestBody VaultChargeRequestDTO vaultChargeRequestDTO);
}
