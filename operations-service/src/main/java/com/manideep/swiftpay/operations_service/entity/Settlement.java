package com.manideep.swiftpay.operations_service.entity;

import com.manideep.swiftpay.common_lib.entity.Audit;
import com.manideep.swiftpay.common_lib.entity.Money;
import com.manideep.swiftpay.common_lib.enums.SettlementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlements")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settlement extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gross_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "gross_currency"))
    })
    private Money grossAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "gst_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "gst_currency"))
    })
    private Money gstAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "fee_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "fee_currency"))
    })
    private Money feeAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "net_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "net_currency"))
    })
    private Money netAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits", column = @Column(name = "refund_amount_units")),
            @AttributeOverride(name = "currency", column = @Column(name = "refund_currency"))
    })
    private Money refundAmount;

    @Column(nullable = false)
    private String bankReference;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SettlementStatus status;

    private LocalDateTime processedAt;

    private String failureReason;
}
