package com.manideep.swiftpay.payment_service.processor.strategy;

import com.manideep.swiftpay.common_lib.dto.PaymentProcessorRequest;
import com.manideep.swiftpay.common_lib.dto.PaymentProcessorResponse;
import com.manideep.swiftpay.common_lib.util.RandomizerUtil;
import com.manideep.swiftpay.payment_service.processor.PaymentProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NetBankingPaymentProcessor implements PaymentProcessor {


    @Override
    public PaymentProcessorResponse charge(PaymentProcessorRequest request) {
        log.info("Charging money from bank with payment id : {}", request.paymentId());

        final String BANK_CODE_FAIL = "BANK_CODE_FAIL";

        String bankCode = request.methodDetails() != null ? request.methodDetails().get("BANK").toString() : null;

        if(BANK_CODE_FAIL.equals(bankCode)){
            return new PaymentProcessorResponse.Failed("BANK_REJECTED", "Bank rejected the payment");
        }

        String processorRef = "NB_PROCESSOR_"+ RandomizerUtil.randomBase64(16);
        String redirectRef = "http://REDIRECT_BANK.com/"+processorRef;

        return new PaymentProcessorResponse.Success(processorRef,redirectRef);
    }
}
