package com.manideep.swiftpay.operations_service.entity;

import com.manideep.swiftpay.common_lib.entity.Audit;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "settlement_payments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementPayment extends Audit {

    @EmbeddedId
    private SettlementPaymentId id;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;
}
