package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Approval Delegation Entity
 * 결재 위임 엔티티 (휴가/출장 시 결재 권한 위임)
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_approval_delegations",
    indexes = {
        @Index(name = "idx_sd_approval_delegation_delegator", columnList = "delegator_id"),
        @Index(name = "idx_sd_approval_delegation_delegate", columnList = "delegate_id"),
        @Index(name = "idx_sd_approval_delegation_date", columnList = "start_date, end_date"),
        @Index(name = "idx_sd_approval_delegation_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalDelegationEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delegation_id")
    private Long delegationId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    // Delegator Information (위임자 - 원래 승인자)
    @Column(name = "delegator_id", nullable = false)
    private Long delegatorId;

    @Column(name = "delegator_name", length = 100)
    private String delegatorName;

    // Delegate Information (수임자 - 대신 승인할 사람)
    @Column(name = "delegate_id", nullable = false)
    private Long delegateId;

    @Column(name = "delegate_name", length = 100)
    private String delegateName;

    // Delegation Settings
    @Column(name = "delegation_type", nullable = false, length = 20)
    @Builder.Default
    private String delegationType = "FULL";  // FULL, PARTIAL

    @Column(name = "document_types", columnDefinition = "TEXT")
    private String documentTypes;  // JSON array of document types

    // Delegation Period
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    // Status
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "delegation_reason", columnDefinition = "TEXT")
    private String delegationReason;

    /**
     * Check if this is a full delegation
     */
    public boolean isFullDelegation() {
        return "FULL".equals(delegationType);
    }

    /**
     * Check if this is a partial delegation
     */
    public boolean isPartialDelegation() {
        return "PARTIAL".equals(delegationType);
    }

    /**
     * Check if delegation is currently effective
     */
    public boolean isEffective() {
        if (!isActive) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * Check if delegation is effective on specific date
     */
    public boolean isEffectiveOn(LocalDate date) {
        if (!isActive) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Check if delegation covers document type
     */
    public boolean coversDocumentType(String documentType) {
        if (isFullDelegation()) {
            return true;
        }
        if (documentTypes == null || documentTypes.isEmpty()) {
            return false;
        }
        // Simple check - in real implementation, parse JSON array
        return documentTypes.contains(documentType);
    }

    /**
     * Check if delegation is expired
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Check if delegation has started
     */
    public boolean hasStarted() {
        return !LocalDate.now().isBefore(startDate);
    }

    /**
     * Check if delegation is upcoming
     */
    public boolean isUpcoming() {
        return LocalDate.now().isBefore(startDate);
    }

    /**
     * Get remaining days
     */
    public long getRemainingDays() {
        if (isExpired()) {
            return 0;
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return java.time.temporal.ChronoUnit.DAYS.between(today, startDate);
        }
        return java.time.temporal.ChronoUnit.DAYS.between(today, endDate);
    }

    /**
     * Get total duration in days
     */
    public long getTotalDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Activate delegation
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Deactivate delegation
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Extend delegation period
     */
    public void extendTo(LocalDate newEndDate) {
        if (newEndDate.isAfter(this.endDate)) {
            this.endDate = newEndDate;
        }
    }

    /**
     * Check if can delegate to this user for document type
     */
    public boolean canDelegateFor(String documentType, LocalDate date) {
        return isActive && isEffectiveOn(date) && coversDocumentType(documentType);
    }
}
