package com.manideep.swiftpay.payment_service.entity;

import com.manideep.swiftpay.common_lib.entity.Audit;
import com.manideep.swiftpay.common_lib.enums.PaymentActor;
import com.manideep.swiftpay.common_lib.enums.PaymentEvent;
import com.manideep.swiftpay.common_lib.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transition_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentTransitionLog extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus fromStatus;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private PaymentStatus toStatus;

    @Column(length = 100)
    @Enumerated(EnumType.STRING)
    private PaymentActor actor;

    @Column(nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false, length = 20)
    private PaymentEvent paymentEvent;
}
