package com.manideep.swiftpay.merchant_service.controller;

import com.manideep.swiftpay.common_lib.dto.CustomerRequestDTO;
import com.manideep.swiftpay.merchant_service.api.CustomerLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/customers")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final CustomerLookupService customerLookupService;

    @PostMapping("/find-or-create")
    public UUID findOrCreate(@RequestBody CustomerRequestDTO customerRequestDTO){
        return customerLookupService.findOrCreateCustomer(customerRequestDTO);
    }
}
