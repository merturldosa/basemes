package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Supplier Entity
 * 공급업체 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "business",
    name = "sd_suppliers",
    indexes = {
        @Index(name = "idx_supplier_tenant", columnList = "tenant_id"),
        @Index(name = "idx_supplier_type", columnList = "supplier_type"),
        @Index(name = "idx_supplier_active", columnList = "is_active"),
        @Index(name = "idx_supplier_name", columnList = "supplier_name"),
        @Index(name = "idx_supplier_rating", columnList = "rating")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_supplier_code", columnNames = {"tenant_id", "supplier_code"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Long supplierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "supplier_code", nullable = false, length = 50)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 200)
    private String supplierName;

    @Column(name = "supplier_type", nullable = false, length = 20)
    private String supplierType;  // MATERIAL, SERVICE, EQUIPMENT, BOTH

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

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "tax_type", length = 20)
    private String taxType;  // TAXABLE, EXEMPT, ZERO_RATE

    @Column(name = "lead_time_days")
    private Integer leadTimeDays;

    @Column(name = "min_order_amount", precision = 15, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "rating", length = 20)
    private String rating;  // EXCELLENT, GOOD, AVERAGE, POOR

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
