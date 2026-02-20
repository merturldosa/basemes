package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDate;

/**
 * Gauge Entity
 * 계측기 마스터 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_gauges",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_gauge_code", columnNames = {"tenant_id", "gauge_code"})
    },
    indexes = {
        @Index(name = "idx_gauge_tenant", columnList = "tenant_id"),
        @Index(name = "idx_gauge_equipment", columnList = "equipment_id"),
        @Index(name = "idx_gauge_cal_status", columnList = "calibration_status"),
        @Index(name = "idx_gauge_next_cal", columnList = "next_calibration_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GaugeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gauge_id")
    private Long gaugeId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "gauge_code", nullable = false, length = 50)
    private String gaugeCode;

    @Column(name = "gauge_name", nullable = false, length = 200)
    private String gaugeName;

    @Column(name = "gauge_type", length = 50)
    private String gaugeType;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "serial_no", length = 100)
    private String serialNo;

    // Equipment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private EquipmentEntity equipment;

    // Department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private DepartmentEntity department;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "measurement_range", length = 100)
    private String measurementRange;

    @Column(name = "accuracy", length = 50)
    private String accuracy;

    // Calibration
    @Column(name = "calibration_cycle_days")
    private Integer calibrationCycleDays;

    @Column(name = "last_calibration_date")
    private LocalDate lastCalibrationDate;

    @Column(name = "next_calibration_date")
    private LocalDate nextCalibrationDate;

    @Column(name = "calibration_status", length = 30)
    private String calibrationStatus; // VALID, EXPIRED, IN_CALIBRATION

    // Status
    @Column(name = "status", nullable = false, length = 30)
    private String status; // ACTIVE, INACTIVE, DISPOSED

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active")
    private Boolean isActive;
}
