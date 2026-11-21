package com.finup.asset;

import com.finup.assetgroup.AssetGroup;
import com.finup.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "assets", schema = "finup")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private AssetGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(length = 150)
    private String institution;

    /**
     * Primeiro mês (sempre dia 1) em que o ativo deixa de existir.
     * Nulo = ativo vigente.
     */
    @Column(name = "liquidated_at")
    private LocalDate liquidatedAt;

    /**
     * Coluna legada. Espelha {@code liquidatedAt == null} apenas para permitir
     * rollback do backend; não deve ser lida. Remover junto com a coluna (V7).
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.active = liquidatedAt == null;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        this.active = liquidatedAt == null;
    }

    public boolean isLiquidated() {
        return liquidatedAt != null;
    }

    /** Ativo liquidado não aceita lançamentos do mês de liquidação em diante. */
    public boolean acceptsEntriesIn(YearMonth period) {
        return liquidatedAt == null || period.isBefore(YearMonth.from(liquidatedAt));
    }
}
