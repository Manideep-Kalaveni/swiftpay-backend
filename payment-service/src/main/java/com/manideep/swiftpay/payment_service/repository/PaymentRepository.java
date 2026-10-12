package com.manideep.swiftpay.payment_service.repository;

import com.manideep.swiftpay.common_lib.enums.PaymentStatus;
import com.manideep.swiftpay.payment_service.entity.Payment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByOrder_Id(UUID id);

    Optional<Payment> findByIdAndMerchantId(UUID paymentId, UUID merchantId);

    List<Payment> findByPaymentStatusAndCreatedAtBefore(PaymentStatus paymentStatus, LocalDateTime globalWindow);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p from Payment p WHERE p.merchantId = :merchantId AND p.paymentStatus = :paymentStatus")
    List<Payment> findByMerchantIdAndPaymentStatusForUpdate(@Param("merchantId") UUID merchantId,@Param("paymentStatus") PaymentStatus paymentStatus);

    Optional<Payment> findByMerchantIdAndIdempotencyKey(UUID merchantId, String idempotencyKey);
}
