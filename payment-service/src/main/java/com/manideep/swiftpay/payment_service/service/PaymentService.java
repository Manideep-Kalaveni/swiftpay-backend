package com.manideep.swiftpay.payment_service.service;

import com.manideep.swiftpay.payment_service.dto.request.PaymentRequestDTO;
import com.manideep.swiftpay.payment_service.dto.response.PaymentResponseDTO;

import java.util.UUID;

public interface PaymentService {
    PaymentResponseDTO createPayment(UUID merchantId, PaymentRequestDTO paymentRequestDTO, String idempotencyKey);

    PaymentResponseDTO capture(UUID merchantId, UUID paymentId);

    void resolveAuthorization(UUID id, boolean b, String bankRef, String simBankErrorCode, String simulatorBankDeclined);
}
