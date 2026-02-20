package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equipment Entity
 * 설비 마스터 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_equipments",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_equipment_code", columnNames = {"tenant_id", "equipment_code"})
    },
    indexes = {
        @Index(name = "idx_equipment_tenant", columnList = "tenant_id"),
        @Index(name = "idx_equipment_status", columnList = "status"),
        @Index(name = "idx_equipment_type", columnList = "equipment_type"),
        @Index(name = "idx_equipment_site", columnList = "site_id"),
        @Index(name = "idx_equipment_department", columnList = "department_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Long equipmentId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "equipment_code", nullable = false, length = 50)
    private String equipmentCode;

    @Column(name = "equipment_name", nullable = false, length = 200)
    private String equipmentName;

    // Type & Classification
    @Column(name = "equipment_type", nullable = false, length = 30)
    private String equipmentType; // MACHINE, MOLD, TOOL, FACILITY, VEHICLE, OTHER

    @Column(name = "equipment_category", length = 50)
    private String equipmentCategory;

    // Manufacturer Info
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "serial_no", length = 100)
    private String serialNo;

    // Purchase & Installation
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "purchase_price", precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "installation_date")
    private LocalDate installationDate;

    @Column(name = "warranty_end_date")
    private LocalDate warrantyEndDate;

    // Location
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private SiteEntity site;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Column(name = "location", length = 200)
    private String location;

    // Specifications
    @Column(name = "capacity", length = 50)
    private String capacity;

    @Column(name = "capacity_unit", length = 20)
    private String capacityUnit;

    @Column(name = "power_rating", precision = 10, scale = 2)
    private BigDecimal powerRating; // kW

    @Column(name = "dimensions", length = 100)
    private String dimensions;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight; // kg

    @Column(name = "specifications", columnDefinition = "TEXT")
    private String specifications;

    // Status
    @Column(name = "status", nullable = false, length = 30)
    private String status; // OPERATIONAL, STOPPED, MAINTENANCE, BREAKDOWN, RETIRED

    @Column(name = "operational_status", length = 30)
    private String operationalStatus; // RUNNING, IDLE, STOPPED

    // Maintenance
    @Column(name = "maintenance_cycle_days")
    private Integer maintenanceCycleDays;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    // Performance
    @Column(name = "standard_cycle_time", precision = 10, scale = 2)
    private BigDecimal standardCycleTime; // seconds

    @Column(name = "actual_oee_target", precision = 5, scale = 2)
    private BigDecimal actualOeeTarget; // percentage

    // Additional
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "manual_url", length = 500)
    private String manualUrl;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
