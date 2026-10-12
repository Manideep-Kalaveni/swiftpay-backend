package com.manideep.swiftpay.vault_service.dto.request;

import com.manideep.swiftpay.vault_service.validation.ExpiryYear;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.LuhnCheck;

import java.util.UUID;

public record TokenizeRequestDTO(
        @NotBlank(message = "PAN is Required")
        @LuhnCheck(message = "Invalid card number")
        @Pattern(regexp = "^[0-9]{13,19}$", message = "PAN length is Invalid")
        String pan,
        @NotBlank(message = "CVV is Required")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV length is Invalid")
        String cvv,
        @NotNull(message = "Expiry Month is Required")
        @Min(value = 1, message = "Expiry Month must be between 1 and 12")
        @Max(value = 12, message = "Expiry Month must be between 1 and 12")
        Integer expiryMonth,
        @NotNull(message = "Expiry Year is Required")
        @ExpiryYear
        Integer expiryYear,
        UUID customerId,
        String cardHolderName
) {
}
