package com.manideep.swiftpay.vault_service.entity;

import com.manideep.swiftpay.common_lib.entity.Audit;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "card_tokens")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardToken extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String token;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private UUID customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vault_card_id", nullable = false)
    private VaultCard vaultCard;

    private LocalDateTime revokedAt;
}
