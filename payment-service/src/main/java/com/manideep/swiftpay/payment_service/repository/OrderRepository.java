package com.manideep.swiftpay.payment_service.repository;

import com.manideep.swiftpay.payment_service.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    boolean existsByMerchantIdAndReceipt(UUID merchantId, String receipt);

    Optional<Order> findByIdAndMerchantId(UUID id, UUID merchantId);
}
