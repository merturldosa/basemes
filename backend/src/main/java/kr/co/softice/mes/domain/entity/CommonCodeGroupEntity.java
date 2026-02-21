package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Common Code Group Entity
 * 공통 코드 그룹 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_common_code_groups",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_code_group_tenant", columnNames = {"tenant_id", "code_group"})
    },
    indexes = {
        @Index(name = "idx_sd_code_group_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_code_group_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonCodeGroupEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_group_id")
    private Long codeGroupId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "code_group", nullable = false, length = 50)
    private String codeGroup;

    @Column(name = "code_group_name", nullable = false, length = 100)
    private String codeGroupName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_system", nullable = false)
    @Builder.Default
    private Boolean isSystem = false;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Relationships
    @OneToMany(mappedBy = "codeGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommonCodeDetailEntity> details = new ArrayList<>();

    // Helper methods
    public void addDetail(CommonCodeDetailEntity detail) {
        details.add(detail);
        detail.setCodeGroup(this);
    }

    public void removeDetail(CommonCodeDetailEntity detail) {
        details.remove(detail);
        detail.setCodeGroup(null);
    }
}
