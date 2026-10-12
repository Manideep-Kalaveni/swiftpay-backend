package com.manideep.swiftpay.merchant_service.controller;

import com.manideep.swiftpay.common_lib.dto.CustomerRequestDTO;
import com.manideep.swiftpay.common_lib.dto.SettlementBankDetailsDTO;
import com.manideep.swiftpay.common_lib.dto.WebhookTargetResponseDTO;
import com.manideep.swiftpay.merchant_service.api.CustomerLookupService;
import com.manideep.swiftpay.merchant_service.api.MerchantLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/merchants")
@RequiredArgsConstructor
public class InternalMerchantController {

    private final MerchantLookupService merchantLookupService;
    private final CustomerLookupService customerLookupService;

    @GetMapping("/{merchantId}/webhook-targets")
    public List<WebhookTargetResponseDTO> getActiveConfigsForEvent(@PathVariable UUID merchantId, @RequestParam String eventType){
        return merchantLookupService.getActiveConfigsForEvent(merchantId,eventType);
    }

    @GetMapping("/active-ids")
    public List<UUID> listActiveMerchantIds(){
        return merchantLookupService.getActiveMerchants();
    }

    @GetMapping("/{merchantId}/settlement-bank-details")
    public SettlementBankDetailsDTO getSettlementBankDetails(@PathVariable UUID merchantId){
        return merchantLookupService.getSettlementBankDetails(merchantId);
    }

    @PostMapping("/find-or-create")
    public UUID findOrCreate(@RequestBody CustomerRequestDTO customerRequestDTO){
        return customerLookupService.findOrCreateCustomer(customerRequestDTO);
    }
}
