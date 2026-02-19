package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equipment Part Entity
 * 설비 부품 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "si_equipment_parts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_eq_part_code", columnNames = {"tenant_id", "equipment_id", "part_code"})
    },
    indexes = {
        @Index(name = "idx_eq_part_tenant", columnList = "tenant_id"),
        @Index(name = "idx_eq_part_equipment", columnList = "equipment_id"),
        @Index(name = "idx_eq_part_status", columnList = "status"),
        @Index(name = "idx_eq_part_next_replace", columnList = "next_replacement_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentPartEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "part_id")
    private Long partId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipment;

    @Column(name = "part_code", nullable = false, length = 50)
    private String partCode;

    @Column(name = "part_name", nullable = false, length = 200)
    private String partName;

    @Column(name = "part_type", length = 50)
    private String partType;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "serial_no", length = 100)
    private String serialNo;

    // Lifecycle
    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "expected_life_days")
    private Integer expectedLifeDays;

    @Column(name = "replacement_date")
    private LocalDate replacementDate;

    @Column(name = "next_replacement_date")
    private LocalDate nextReplacementDate;

    @Builder.Default
    @Column(name = "replacement_count")
    private Integer replacementCount = 0;

    // Cost
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;

    // Status
    @Column(name = "status", nullable = false, length = 30)
    private String status; // ACTIVE, WORN, REPLACED, DISPOSED

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
