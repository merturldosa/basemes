package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Customer Entity
 * 고객 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "business",
    name = "si_customers",
    indexes = {
        @Index(name = "idx_customer_tenant", columnList = "tenant_id"),
        @Index(name = "idx_customer_type", columnList = "customer_type"),
        @Index(name = "idx_customer_active", columnList = "is_active"),
        @Index(name = "idx_customer_name", columnList = "customer_name")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_code", columnNames = {"tenant_id", "customer_code"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "customer_code", nullable = false, length = 50)
    private String customerCode;

    @Column(name = "customer_name", nullable = false, length = 200)
    private String customerName;

    @Column(name = "customer_type", nullable = false, length = 20)
    private String customerType;  // DOMESTIC, OVERSEAS, BOTH

    @Column(name = "business_number", length = 50)
    private String businessNumber;

    @Column(name = "representative_name", length = 100)
    private String representativeName;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "phone_number", length = 50)
    private String phoneNumber;

    @Column(name = "fax_number", length = 50)
    private String faxNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "website", length = 200)
    private String website;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "payment_terms", length = 20)
    private String paymentTerms;  // NET30, NET60, COD, ADVANCE

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "tax_type", length = 20)
    private String taxType;  // TAXABLE, EXEMPT, ZERO_RATE

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
