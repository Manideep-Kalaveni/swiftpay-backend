package com.manideep.swiftpay.operations_service.settlement;

import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.operations_service.settlement.dto.BankTransferResult;

import java.util.UUID;

public interface BankTransferProcessor {
    BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc);
}