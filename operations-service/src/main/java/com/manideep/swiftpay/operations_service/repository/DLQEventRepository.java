package com.manideep.swiftpay.operations_service.repository;

import com.manideep.swiftpay.operations_service.entity.DLQEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DLQEventRepository extends JpaRepository<DLQEvent, UUID> {
}
