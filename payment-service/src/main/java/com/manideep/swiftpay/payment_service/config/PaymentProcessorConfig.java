package com.manideep.swiftpay.payment_service.config;

import com.manideep.swiftpay.common_lib.enums.PaymentMethod;
import com.manideep.swiftpay.payment_service.processor.PaymentProcessor;
import com.manideep.swiftpay.payment_service.processor.strategy.CardPaymentProcessor;
import com.manideep.swiftpay.payment_service.processor.strategy.NetBankingPaymentProcessor;
import com.manideep.swiftpay.payment_service.processor.strategy.UpiPaymentProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class PaymentProcessorConfig {

    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(PaymentMethod.CARD, new CardPaymentProcessor(),
                PaymentMethod.UPI, new UpiPaymentProcessor(),
                PaymentMethod.NET_BANKING, new NetBankingPaymentProcessor());
    }
}
