package com.manideep.swiftpay.merchant_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(
        @NotBlank(message = "Email is Required")
        @Email(message = "Email should be valid email")
        String email,
        @NotBlank(message = "Password is Required")
        @Size(min = 8, message = "Password should be at least 8 characters")
        String password
) {
}
