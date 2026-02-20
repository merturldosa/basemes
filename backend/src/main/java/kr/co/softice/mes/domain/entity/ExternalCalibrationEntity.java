package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * External Calibration Entity
 * 외부 검교정 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_external_calibrations",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_calibration_no", columnNames = {"tenant_id", "calibration_no"})
    },
    indexes = {
        @Index(name = "idx_ext_cal_tenant", columnList = "tenant_id"),
        @Index(name = "idx_ext_cal_gauge", columnList = "gauge_id"),
        @Index(name = "idx_ext_cal_status", columnList = "status"),
        @Index(name = "idx_ext_cal_requested_date", columnList = "requested_date"),
        @Index(name = "idx_ext_cal_result", columnList = "calibration_result")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExternalCalibrationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calibration_id")
    private Long calibrationId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "calibration_no", nullable = false, length = 50)
    private String calibrationNo;

    // Gauge
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gauge_id", nullable = false)
    private GaugeEntity gauge;

    @Column(name = "calibration_vendor", length = 200)
    private String calibrationVendor;

    @Column(name = "requested_date", nullable = false)
    private LocalDate requestedDate;

    @Column(name = "sent_date")
    private LocalDate sentDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "certificate_no", length = 100)
    private String certificateNo;

    @Column(name = "certificate_url", length = 500)
    private String certificateUrl;

    @Column(name = "calibration_result", length = 30)
    private String calibrationResult; // PASS, FAIL, CONDITIONAL

    @Column(name = "cost", precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(name = "next_calibration_date")
    private LocalDate nextCalibrationDate;

    @Column(name = "status", nullable = false, length = 30)
    private String status; // REQUESTED, SENT, IN_PROGRESS, COMPLETED

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
