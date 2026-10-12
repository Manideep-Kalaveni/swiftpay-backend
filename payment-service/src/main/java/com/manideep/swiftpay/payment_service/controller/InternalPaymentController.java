package com.manideep.swiftpay.payment_service.controller;

import com.manideep.swiftpay.common_lib.dto.PaymentSettlementView;
import com.manideep.swiftpay.payment_service.api.PaymentLookupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentLookupService paymentLookupService;

    @GetMapping("/unsettled-captured")
    public List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId){
        return paymentLookupService.findUnsettledCapturedPayments(merchantId);
    }

    @PostMapping("/mark-settled")
    public void markSettled(@RequestBody List<UUID> paymentIds){
        paymentLookupService.markSettled(paymentIds);
    }
}
