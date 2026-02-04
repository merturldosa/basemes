package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * BOM Detail Entity
 * BOM 상세 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "bom",
    name = "si_bom_details",
    indexes = {
        @Index(name = "idx_bom_detail_bom", columnList = "bom_id"),
        @Index(name = "idx_bom_detail_material", columnList = "material_product_id"),
        @Index(name = "idx_bom_detail_process", columnList = "process_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_bom_detail_sequence", columnNames = {"bom_id", "sequence"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BomDetailEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bom_detail_id")
    private Long bomDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BomEntity bom;

    @Column(name = "sequence", nullable = false)
    private Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_product_id", nullable = false)
    private ProductEntity materialProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "process_id")
    private ProcessEntity process;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "usage_rate", precision = 5, scale = 2)
    private BigDecimal usageRate;

    @Column(name = "scrap_rate", precision = 5, scale = 2)
    private BigDecimal scrapRate;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
