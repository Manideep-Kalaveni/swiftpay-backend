package com.manideep.swiftpay.payment_service.controller;

import com.manideep.swiftpay.payment_service.dto.request.PaymentRequestDTO;
import com.manideep.swiftpay.payment_service.dto.response.PaymentResponseDTO;
import com.manideep.swiftpay.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    UUID merchantId = UUID.fromString("ab57a108-c838-458a-a292-813b643763fa");


    @PostMapping("/init")
    public ResponseEntity<PaymentResponseDTO> createPaymentRequest(@RequestBody @Valid PaymentRequestDTO paymentRequestDTO, @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey){
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(merchantId,paymentRequestDTO,idempotencyKey));
    }

    @PostMapping("/{paymentId}/capture")
    public ResponseEntity<PaymentResponseDTO> capturePayment(@PathVariable UUID paymentId){
        return ResponseEntity.ok(paymentService.capture(merchantId,paymentId));
    }
}
