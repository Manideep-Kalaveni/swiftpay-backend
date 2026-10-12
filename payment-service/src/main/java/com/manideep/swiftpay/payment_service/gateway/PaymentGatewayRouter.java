package com.manideep.swiftpay.payment_service.gateway;

import com.manideep.swiftpay.common_lib.enums.PaymentMethod;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentRequest;
import com.manideep.swiftpay.payment_service.gateway.dto.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentGatewayRouter {

    private final Map<PaymentMethod, PaymentAdapter> paymentAdapters;

    public PaymentResult initiate(PaymentRequest request){
        PaymentAdapter paymentAdapter = paymentAdapters.get(request.method());

        return paymentAdapter.initiate(request);
    }

    public PaymentResult capture(PaymentMethod paymentMethod, UUID paymentId) {
        PaymentAdapter paymentAdapter = paymentAdapters.get(paymentMethod);

        return paymentAdapter.capture(paymentId);
    }
}
