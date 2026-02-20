package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import java.util.Map;

/**
 * Tenant Entity - 테넌트 (회사/사업장)
 * Maps to: common.SD_Tenants
 *
 * @author Moon Myung-seop
 */
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Entity
@Table(name = "sd_tenants", schema = "common")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantEntity extends BaseEntity {

    @Id
    @Column(name = "tenant_id", length = 50)
    private String tenantId;

    @Column(name = "tenant_name", nullable = false, length = 200)
    private String tenantName;

    @Column(name = "tenant_code", nullable = false, length = 50, unique = true)
    private String tenantCode;

    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;

    @Column(name = "industry_type", nullable = false, length = 50)
    private String industryType;

    /**
     * 테넌트별 선택된 테마
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private ThemeEntity theme;

    @Type(type = "jsonb")
    @Column(name = "config", columnDefinition = "jsonb")
    private Map<String, Object> config;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "description", length = 500)
    private String description;
}
