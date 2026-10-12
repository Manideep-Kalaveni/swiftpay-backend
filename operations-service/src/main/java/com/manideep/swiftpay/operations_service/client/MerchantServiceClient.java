package com.manideep.swiftpay.operations_service.client;

import com.manideep.swiftpay.common_lib.dto.SettlementBankDetailsDTO;
import com.manideep.swiftpay.common_lib.dto.WebhookTargetResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "merchant-service", path = "/internal/merchants")
public interface MerchantServiceClient {

    @GetMapping("/{merchantId}/webhook-targets")
    List<WebhookTargetResponseDTO> getActiveConfigsForEvent(@PathVariable UUID merchantId, @RequestParam String eventType);

    @GetMapping("/active-ids")
    List<UUID> listActiveMerchantIds();

    @GetMapping("/{merchantId}/settlement-bank-details")
    SettlementBankDetailsDTO getSettlementBankDetails(@PathVariable UUID merchantId);
}
