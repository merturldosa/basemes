package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Site Entity
 * 사업장
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    schema = "core",
    name = "si_sites",
    indexes = {
        @Index(name = "idx_site_tenant", columnList = "tenant_id"),
        @Index(name = "idx_site_active", columnList = "is_active"),
        @Index(name = "idx_site_type", columnList = "site_type")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_site_code", columnNames = {"tenant_id", "site_code"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SiteEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "site_id")
    private Long siteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "site_code", nullable = false, length = 50)
    private String siteCode;

    @Column(name = "site_name", nullable = false, length = 200)
    private String siteName;

    // Location
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "region", length = 100)
    private String region;

    // Contact
    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "fax", length = 50)
    private String fax;

    @Column(name = "email", length = 100)
    private String email;

    // Manager
    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "manager_phone", length = 50)
    private String managerPhone;

    @Column(name = "manager_email", length = 100)
    private String managerEmail;

    // Type & Status
    @Column(name = "site_type", length = 30)
    private String siteType;  // FACTORY, WAREHOUSE, OFFICE, RD_CENTER

    @Column(name = "is_active")
    private Boolean isActive;

    // Additional Info
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
