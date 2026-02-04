package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Purchase Request Entity
 * 구매 요청
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "purchase",
    name = "si_purchase_requests",
    indexes = {
        @Index(name = "idx_purchase_request_tenant", columnList = "tenant_id"),
        @Index(name = "idx_purchase_request_status", columnList = "status"),
        @Index(name = "idx_purchase_request_material", columnList = "material_id"),
        @Index(name = "idx_purchase_request_date", columnList = "request_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_purchase_request_no", columnNames = {"tenant_id", "request_no"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequestEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_request_id")
    private Long purchaseRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "request_no", nullable = false, length = 50)
    private String requestNo;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private UserEntity requester;

    @Column(name = "department", length = 100)
    private String department;

    // Request Details
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private MaterialEntity material;

    @Column(name = "requested_quantity", nullable = false, precision = 15, scale = 3)
    private BigDecimal requestedQuantity;

    @Column(name = "required_date")
    private LocalDateTime requiredDate;

    @Column(name = "purpose", length = 500)
    private String purpose;

    // Approval
    @Column(name = "status", nullable = false, length = 20)
    private String status;  // PENDING, APPROVED, REJECTED, ORDERED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_user_id")
    private UserEntity approver;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approval_comment", columnDefinition = "TEXT")
    private String approvalComment;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
