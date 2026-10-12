package com.manideep.swiftpay.operations_service.settlement;

import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.common_lib.util.RandomizerUtil;
import com.manideep.swiftpay.operations_service.settlement.dto.BankTransferResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class BankTransferProcessorImpl implements BankTransferProcessor {
    @Override
    public BankTransferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc) {
        // Call the Bank API

        String registrationRef = "TXN_"+ RandomizerUtil.randomBase64(12);
        log.debug("Bank Transfer call completed for settlement id :{}, registration reference :{}", settlementId, registrationRef);
        return new BankTransferResult(registrationRef);
    }
}