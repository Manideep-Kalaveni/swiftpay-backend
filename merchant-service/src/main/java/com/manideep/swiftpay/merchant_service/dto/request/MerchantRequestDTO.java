package com.manideep.swiftpay.merchant_service.dto.request;

import com.manideep.swiftpay.common_lib.enums.BusinessType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MerchantRequestDTO(
        @NotBlank(message = "Name is Required")
        @Size(max = 25 , message = "Name should not be more than 25 characters")
        String name,
        @NotBlank(message = "Password is Required")
        String password,
        @NotBlank(message = "Email is Required")
        @Email(message = "Email should be a valid one")
        String email,
        @Size(max = 25, message = "Business name should not be more than 25 characters")
        String businessName,
        BusinessType businessType
) {
}
