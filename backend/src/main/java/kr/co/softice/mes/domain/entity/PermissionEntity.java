package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Permission Entity - 권한
 * Maps to: common.SI_Permissions
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_permissions",
    schema = "common",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_si_permissions_code",
            columnNames = {"permission_code"}
        )
    },
    indexes = {
        @Index(name = "idx_si_permissions_module", columnList = "module")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long permissionId;

    @Column(name = "permission_code", nullable = false, length = 100)
    private String permissionCode;

    @Column(name = "permission_name", nullable = false, length = 200)
    private String permissionName;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "active";
}
