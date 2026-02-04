package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Role-Permission Entity - 역할-권한 매핑
 * Maps to: common.SI_RolePermissions
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_role_permissions",
    schema = "common",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_si_role_permissions",
            columnNames = {"role_id", "permission_id"}
        )
    },
    indexes = {
        @Index(name = "idx_si_role_permissions_role_id", columnList = "role_id"),
        @Index(name = "idx_si_role_permissions_permission_id", columnList = "permission_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermissionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_permission_id")
    private Long rolePermissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_role_permissions_role_id"))
    private RoleEntity role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_role_permissions_permission_id"))
    private PermissionEntity permission;
}
