package com.manideep.swiftpay.payment_service.service;

import com.manideep.swiftpay.payment_service.dto.request.OrderRequestDTO;
import com.manideep.swiftpay.payment_service.dto.response.OrderResponseDTO;
import com.manideep.swiftpay.payment_service.dto.response.PaymentResponseDTO;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponseDTO createOrder(UUID merchantId, OrderRequestDTO orderRequestDTO);

    OrderResponseDTO getOrderById(UUID merchantId, UUID id);

    OrderResponseDTO cancelOrderById(UUID merchantId, UUID id);

    List<PaymentResponseDTO> getPaymentsByOrderId(UUID merchantId, UUID id);
}
