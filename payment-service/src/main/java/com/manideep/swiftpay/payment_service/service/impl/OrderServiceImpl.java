package com.manideep.swiftpay.payment_service.service.impl;

import com.manideep.swiftpay.common_lib.dto.CustomerRequestDTO;
import com.manideep.swiftpay.common_lib.enums.EventAggregateType;
import com.manideep.swiftpay.common_lib.enums.OrderStatus;
import com.manideep.swiftpay.common_lib.exception.AlreadyExistsException;
import com.manideep.swiftpay.common_lib.exception.BusinessRuleViolationException;
import com.manideep.swiftpay.common_lib.exception.ResourceNotFoundException;
import com.manideep.swiftpay.payment_service.client.CustomerServiceClient;
import com.manideep.swiftpay.payment_service.dto.request.OrderRequestDTO;
import com.manideep.swiftpay.payment_service.dto.response.OrderResponseDTO;
import com.manideep.swiftpay.payment_service.dto.response.PaymentResponseDTO;
import com.manideep.swiftpay.payment_service.entity.Order;
import com.manideep.swiftpay.payment_service.entity.Payment;
import com.manideep.swiftpay.payment_service.mapper.OrderMapper;
import com.manideep.swiftpay.payment_service.mapper.PaymentMapper;
import com.manideep.swiftpay.payment_service.outbox.OutboxEventPublisher;
import com.manideep.swiftpay.payment_service.repository.OrderRepository;
import com.manideep.swiftpay.payment_service.repository.PaymentRepository;
import com.manideep.swiftpay.payment_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OutboxEventPublisher outboxEventPublisher;
    private final CustomerServiceClient customerServiceClient;

    @Value("${default.expiration.time:30}")
    private Long defaultExpirationTime;

    @Override
    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")
    @Transactional
    public OrderResponseDTO createOrder(UUID merchantId, OrderRequestDTO orderRequestDTO) {

        if(orderRequestDTO.receipt() != null && orderRepository.existsByMerchantIdAndReceipt(merchantId,orderRequestDTO.receipt())){
            throw new AlreadyExistsException("Order already present with receipt " + orderRequestDTO.receipt(), "ORDER_ALREADY_PRESENT");
        }

        UUID customerId = null;

        if(orderRequestDTO.customer() != null){
            customerId = customerServiceClient.findOrCreate(new CustomerRequestDTO(
                    merchantId,
                    orderRequestDTO.customer().name(),
                    orderRequestDTO.customer().email(),
                    orderRequestDTO.customer().phoneNumber()
            ));
        }

        Order order = Order.builder()
                .merchantId(merchantId)
                .customerId(customerId)
                .receipt(orderRequestDTO.receipt())
                .money(orderRequestDTO.amount())
                .notes(orderRequestDTO.notes())
                .expiresAt(orderRequestDTO.expiresAt() != null ? orderRequestDTO.expiresAt() : LocalDateTime.now().plusMinutes(defaultExpirationTime))
                .build();

        order = orderRepository.save(order);


        // TODO Add Kafka for publishing event to another microservice for decoupling

        outboxEventPublisher.publish(EventAggregateType.ORDER, order.getId(), "ORDER_CREATED",
                Map.of("orderId", order.getId(),
                        "merchantId", order.getMerchantId(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getMoney().getAmountUnits(),
                        "amountCurrency", order.getMoney().getCurrency()
                )
                );

        return orderMapper.toOrderResponseDTO(order);
    }

    @Override
    public OrderResponseDTO getOrderById(UUID merchantId, UUID id) {
        Order order = getOrderForMerchant(merchantId,id);
        return orderMapper.toOrderResponseDTO(order);
    }

    @Override
    public OrderResponseDTO cancelOrderById(UUID merchantId, UUID id) {
        Order order = getOrderForMerchant(merchantId,id);

        if(order.getOrderStatus().equals(OrderStatus.CANCELLED) || order.getOrderStatus().equals(OrderStatus.PAID)){
            throw new BusinessRuleViolationException("Order can not be cancelled with status "+ order.getOrderStatus().name(), "ORDER_NOT_CANCELLED");
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        order = orderRepository.save(order);

        // TODO add kafka event for cancelling the order
        outboxEventPublisher.publish(EventAggregateType.ORDER, order.getId(), "ORDER_CANCELLED",
                Map.of("orderId", order.getId(),
                        "merchantId", order.getMerchantId(),
                        "orderStatus", order.getOrderStatus().name(),
                        "amountUnits", order.getMoney().getAmountUnits(),
                        "amountCurrency", order.getMoney().getCurrency()
                )
        );

        return orderMapper.toOrderResponseDTO(order);
    }

    @Override
    public List<PaymentResponseDTO> getPaymentsByOrderId(UUID merchantId, UUID id) {
        Order order = getOrderForMerchant(merchantId, id);

        List<Payment> payments = paymentRepository.findByOrder_Id(order.getId());

        return paymentMapper.toPaymentResponseListDTO(payments);
    }


    private Order getOrderForMerchant(UUID merchantId, UUID orderId){
        return orderRepository.findByIdAndMerchantId(orderId,merchantId).orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    }
}
