package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * BOM Entity
 * BOM 마스터 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "bom",
    name = "si_boms",
    indexes = {
        @Index(name = "idx_bom_tenant", columnList = "tenant_id"),
        @Index(name = "idx_bom_product", columnList = "product_id"),
        @Index(name = "idx_bom_effective_date", columnList = "effective_date"),
        @Index(name = "idx_bom_active", columnList = "is_active")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_bom_code_version", columnNames = {"tenant_id", "bom_code", "version"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bom_id")
    private Long bomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "bom_code", nullable = false, length = 50)
    private String bomCode;

    @Column(name = "bom_name", nullable = false, length = 200)
    private String bomName;

    @Column(name = "version", nullable = false, length = 20)
    private String version;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    @Builder.Default
    private List<BomDetailEntity> details = new ArrayList<>();

    // Helper methods for managing bidirectional relationship
    public void addDetail(BomDetailEntity detail) {
        details.add(detail);
        detail.setBom(this);
    }

    public void removeDetail(BomDetailEntity detail) {
        details.remove(detail);
        detail.setBom(null);
    }

    public void clearDetails() {
        details.forEach(detail -> detail.setBom(null));
        details.clear();
    }
}
