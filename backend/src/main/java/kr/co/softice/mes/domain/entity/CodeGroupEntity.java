package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Code Group Entity - 코드 그룹
 * Maps to: common.SD_CodeGroups
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_code_groups",
    schema = "common",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_sd_code_groups_tenant_code",
            columnNames = {"tenant_id", "group_code"}
        )
    },
    indexes = {
        @Index(name = "idx_sd_code_groups_tenant_id", columnList = "tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeGroupEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long groupId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_code_groups_tenant_id"))
    private TenantEntity tenant;

    @Column(name = "group_code", nullable = false, length = 50)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "active";
}
