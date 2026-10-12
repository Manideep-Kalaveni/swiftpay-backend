package com.manideep.swiftpay.common_lib.dto;

public record SettlementBankDetailsDTO(
        String accountNumber,
        String ifsc,
        String accountHolderName
) {
}
