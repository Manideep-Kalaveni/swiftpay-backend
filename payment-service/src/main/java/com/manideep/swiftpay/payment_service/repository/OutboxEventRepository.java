package com.manideep.swiftpay.payment_service.repository;

import com.manideep.swiftpay.common_lib.enums.OutboxStatus;
import com.manideep.swiftpay.payment_service.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findByStatusOrderByCreatedAt(OutboxStatus outboxStatus);
}
