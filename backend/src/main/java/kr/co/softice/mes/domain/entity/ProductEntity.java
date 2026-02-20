package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Product Entity - 제품 마스터
 * Maps to: mes.SD_Products
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_products",
    schema = "mes",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_sd_products_tenant_code",
            columnNames = {"tenant_id", "product_code"}
        )
    },
    indexes = {
        @Index(name = "idx_sd_products_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_products_code", columnList = "product_code"),
        @Index(name = "idx_sd_products_name", columnList = "product_name")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_products_tenant"))
    private TenantEntity tenant;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_type", length = 50)
    private String productType;  // 완제품, 반제품, 원자재

    @Column(name = "specification", columnDefinition = "TEXT")
    private String specification;

    @Column(name = "unit", nullable = false, length = 20)
    @Builder.Default
    private String unit = "EA";  // EA, KG, L 등

    @Column(name = "standard_cycle_time")
    private Integer standardCycleTime;  // 표준 사이클 타임 (초)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
