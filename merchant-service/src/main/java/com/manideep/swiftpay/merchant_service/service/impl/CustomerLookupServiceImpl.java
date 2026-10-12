package com.manideep.swiftpay.merchant_service.service.impl;

import com.manideep.swiftpay.common_lib.dto.CustomerRequestDTO;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.merchant_service.api.CustomerLookupService;
import com.manideep.swiftpay.merchant_service.entity.Customer;
import com.manideep.swiftpay.merchant_service.entity.Merchant;
import com.manideep.swiftpay.merchant_service.repository.CustomerRepository;
import com.manideep.swiftpay.merchant_service.repository.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerLookupServiceImpl implements CustomerLookupService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;

    @Override
    public UUID findOrCreateCustomer(CustomerRequestDTO customerRequestDTO) {
        UUID merchantId = customerRequestDTO.merchantId();
        String name = customerRequestDTO.name();
        String email = customerRequestDTO.email();
        String phoneNumber = customerRequestDTO.phone();

        if(email == null || email.isBlank()){
            return null;
        }

        return customerRepository.findByMerchant_IdAndEmail(merchantId,email)
                .map(Customer::getId)
                .orElseGet(() -> createNew(merchantId,email,name,phoneNumber));
    }

    private UUID createNew(UUID merchantId, String email, String name, String phoneNumber) {

        Merchant merchant = merchantRepository.findById(merchantId).orElseThrow(() -> new ResourceNotFoundException("Merchant",merchantId));

        Customer customer = Customer.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .merchant(merchant)
                .build();

        customer = customerRepository.save(customer);
        return customer.getId();
    }
}
