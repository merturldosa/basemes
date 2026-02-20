package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import kr.co.softice.mes.domain.entity.BaseEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mold Entity
 * 금형 마스터 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_molds",
        indexes = {
                @Index(name = "idx_mold_tenant", columnList = "tenant_id"),
                @Index(name = "idx_mold_type", columnList = "mold_type"),
                @Index(name = "idx_mold_status", columnList = "status"),
                @Index(name = "idx_mold_site", columnList = "site_id"),
                @Index(name = "idx_mold_department", columnList = "department_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mold_code", columnNames = {"tenant_id", "mold_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoldEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mold_id")
    private Long moldId;

    // Tenant relationship (multi-tenant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "mold_code", length = 50, nullable = false)
    private String moldCode;

    @Column(name = "mold_name", length = 200, nullable = false)
    private String moldName;

    // Classification
    @Column(name = "mold_type", length = 30, nullable = false)
    private String moldType; // INJECTION, PRESS, DIE_CASTING, FORGING, OTHER

    @Column(name = "mold_grade", length = 20)
    private String moldGrade; // A, B, C, S

    @Column(name = "cavity_count")
    private Integer cavityCount;

    // Shot management
    @Column(name = "current_shot_count")
    private Long currentShotCount;

    @Column(name = "max_shot_count")
    private Long maxShotCount;

    @Column(name = "maintenance_shot_interval")
    private Long maintenanceShotInterval;

    @Column(name = "last_maintenance_shot")
    private Long lastMaintenanceShot;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private SiteEntity site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    // Specifications
    @Column(name = "manufacturer", length = 200)
    private String manufacturer;

    @Column(name = "model_name", length = 200)
    private String modelName;

    @Column(name = "serial_no", length = 100)
    private String serialNo;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    // Dates
    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "first_use_date")
    private LocalDate firstUseDate;

    @Column(name = "warranty_period", length = 50)
    private String warrantyPeriod;

    @Column(name = "warranty_expiry_date")
    private LocalDate warrantyExpiryDate;

    // Status
    @Column(name = "status", length = 30, nullable = false)
    private String status; // AVAILABLE, IN_USE, MAINTENANCE, BREAKDOWN, RETIRED

    @Column(name = "location", length = 200)
    private String location;

    // Common fields
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
