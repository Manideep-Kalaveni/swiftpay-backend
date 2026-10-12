package com.manideep.swiftpay.operations_service.settlement;

import com.manideep.swiftpay.common_lib.dto.PaymentSettlementView;
import com.manideep.swiftpay.common_lib.dto.SettlementBankDetailsDTO;
import com.manideep.swiftpay.operations_service.client.MerchantServiceClient;
import com.manideep.swiftpay.operations_service.client.PaymentServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementIntegrationGateway {

    private final MerchantServiceClient merchantServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    @CircuitBreaker(name = "payment-service")
    @Retry(name = "payment-service")
    public List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId){
        return paymentServiceClient.findUnsettledCapturedPayments(merchantId);
    }

    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")
    public SettlementBankDetailsDTO getSettlementBankDetails(@PathVariable UUID merchantId){
        return merchantServiceClient.getSettlementBankDetails(merchantId);
    }

    @CircuitBreaker(name = "payment-service")
    @Retry(name = "payment-service")
    void markSettled(@RequestBody List<UUID> paymentIds){
        paymentServiceClient.markSettled(paymentIds);
    }
}
