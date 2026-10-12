package com.manideep.swiftpay.merchant_service.api;

import com.manideep.swiftpay.common_lib.dto.CustomerRequestDTO;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

public interface CustomerLookupService {
    UUID findOrCreateCustomer(@RequestBody CustomerRequestDTO customerRequestDTO);
}
