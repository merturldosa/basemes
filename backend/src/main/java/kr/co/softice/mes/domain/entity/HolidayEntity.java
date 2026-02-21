package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Holiday Entity
 * 휴일
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_holidays",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_holiday_date", columnNames = {"tenant_id", "holiday_date"})
    },
    indexes = {
        @Index(name = "idx_sd_holiday_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_holiday_date", columnList = "holiday_date"),
        @Index(name = "idx_sd_holiday_type", columnList = "holiday_type"),
        @Index(name = "idx_sd_holiday_active", columnList = "is_active"),
        @Index(name = "idx_sd_holiday_date_range", columnList = "tenant_id, holiday_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HolidayEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holiday_id")
    private Long holidayId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "holiday_name", nullable = false, length = 200)
    private String holidayName;

    @Column(name = "holiday_date", nullable = false)
    private LocalDate holidayDate;

    // Holiday classification
    @Column(name = "holiday_type", nullable = false, length = 50)
    private String holidayType; // NATIONAL, COMPANY, SPECIAL

    @Column(name = "is_recurring")
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "recurrence_rule", length = 100)
    private String recurrenceRule; // YEARLY, MONTHLY, LUNAR, etc.

    // Business day calculation
    @Column(name = "is_working_day")
    @Builder.Default
    private Boolean isWorkingDay = false; // Some holidays may be working days (substitute work days)

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Check if this is a national holiday
     */
    public boolean isNationalHoliday() {
        return "NATIONAL".equals(holidayType);
    }

    /**
     * Check if this is a company holiday
     */
    public boolean isCompanyHoliday() {
        return "COMPANY".equals(holidayType);
    }

    /**
     * Check if this is an actual non-working day
     */
    public boolean isNonWorkingDay() {
        return isActive && !isWorkingDay;
    }

    /**
     * Check if holiday falls on given date
     */
    public boolean fallsOn(LocalDate date) {
        return holidayDate.equals(date);
    }

    /**
     * Check if holiday falls within date range
     */
    public boolean fallsWithin(LocalDate startDate, LocalDate endDate) {
        return !holidayDate.isBefore(startDate) && !holidayDate.isAfter(endDate);
    }
}
