package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Working Hours Entity
 * 근무 시간 설정 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_working_hours",
    indexes = {
        @Index(name = "idx_sd_working_hours_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_working_hours_default", columnList = "is_default"),
        @Index(name = "idx_sd_working_hours_effective", columnList = "tenant_id, effective_from, effective_to")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingHoursEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "working_hours_id")
    private Long workingHoursId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "schedule_name", nullable = false, length = 100)
    private String scheduleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Day of week settings (Monday - Sunday)
    @Column(name = "monday_start")
    private LocalTime mondayStart;

    @Column(name = "monday_end")
    private LocalTime mondayEnd;

    @Column(name = "tuesday_start")
    private LocalTime tuesdayStart;

    @Column(name = "tuesday_end")
    private LocalTime tuesdayEnd;

    @Column(name = "wednesday_start")
    private LocalTime wednesdayStart;

    @Column(name = "wednesday_end")
    private LocalTime wednesdayEnd;

    @Column(name = "thursday_start")
    private LocalTime thursdayStart;

    @Column(name = "thursday_end")
    private LocalTime thursdayEnd;

    @Column(name = "friday_start")
    private LocalTime fridayStart;

    @Column(name = "friday_end")
    private LocalTime fridayEnd;

    @Column(name = "saturday_start")
    private LocalTime saturdayStart;

    @Column(name = "saturday_end")
    private LocalTime saturdayEnd;

    @Column(name = "sunday_start")
    private LocalTime sundayStart;

    @Column(name = "sunday_end")
    private LocalTime sundayEnd;

    // Break times
    @Column(name = "break_start_1")
    private LocalTime breakStart1;

    @Column(name = "break_end_1")
    private LocalTime breakEnd1;

    @Column(name = "break_start_2")
    private LocalTime breakStart2;

    @Column(name = "break_end_2")
    private LocalTime breakEnd2;

    // Effective period
    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    // Default schedule
    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Get working hours for a specific day of week (1=Monday, 7=Sunday)
     */
    public WorkingHours getWorkingHoursForDay(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: // Monday
                return new WorkingHours(mondayStart, mondayEnd);
            case 2: // Tuesday
                return new WorkingHours(tuesdayStart, tuesdayEnd);
            case 3: // Wednesday
                return new WorkingHours(wednesdayStart, wednesdayEnd);
            case 4: // Thursday
                return new WorkingHours(thursdayStart, thursdayEnd);
            case 5: // Friday
                return new WorkingHours(fridayStart, fridayEnd);
            case 6: // Saturday
                return new WorkingHours(saturdayStart, saturdayEnd);
            case 7: // Sunday
                return new WorkingHours(sundayStart, sundayEnd);
            default:
                return null;
        }
    }

    /**
     * Check if this schedule is effective on given date
     */
    public boolean isEffectiveOn(LocalDate date) {
        if (effectiveFrom != null && date.isBefore(effectiveFrom)) {
            return false;
        }
        if (effectiveTo != null && date.isAfter(effectiveTo)) {
            return false;
        }
        return isActive;
    }

    /**
     * Check if given day of week is a working day
     */
    public boolean isWorkingDay(int dayOfWeek) {
        WorkingHours hours = getWorkingHoursForDay(dayOfWeek);
        return hours != null && hours.startTime != null && hours.endTime != null;
    }

    /**
     * Inner class to represent working hours
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class WorkingHours {
        private LocalTime startTime;
        private LocalTime endTime;

        public boolean isWorkingTime(LocalTime time) {
            if (startTime == null || endTime == null) {
                return false;
            }
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        }

        public long getWorkingMinutes() {
            if (startTime == null || endTime == null) {
                return 0;
            }
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
    }
}
