package com.manideep.swiftpay.payment_service.dto.request;

import com.manideep.swiftpay.common_lib.entity.Money;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record OrderRequestDTO(
        @NotNull(message = "Amount is Required")
        Money amount,
        @Size(max = 100)
        String receipt,
        Map<String,Object> notes,
        LocalDateTime expiresAt,
        @Valid
        CustomerDetails customer
) {
        public record CustomerDetails(
                @Size(max = 100)
                String name,

                @Email
                @Size(max = 50)
                String email,

                @Size(max = 20)
                String phoneNumber
        ){

        }
}
