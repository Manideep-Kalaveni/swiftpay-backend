package com.manideep.swiftpay.payment_service.controller;

import com.manideep.swiftpay.payment_service.dto.request.OrderRequestDTO;
import com.manideep.swiftpay.payment_service.dto.response.OrderResponseDTO;
import com.manideep.swiftpay.payment_service.dto.response.PaymentResponseDTO;
import com.manideep.swiftpay.payment_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    UUID merchantId = UUID.fromString("ab57a108-c838-458a-a292-813b643763fa");

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@RequestBody OrderRequestDTO orderRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(merchantId,orderRequestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable UUID id){
        return ResponseEntity.ok(orderService.getOrderById(merchantId,id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> cancelOrderById(@PathVariable UUID id){
        return ResponseEntity.ok(orderService.cancelOrderById(merchantId,id));
    }

    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByOrderId(@PathVariable UUID id){
        return ResponseEntity.ok(orderService.getPaymentsByOrderId(merchantId,id));
    }
}
