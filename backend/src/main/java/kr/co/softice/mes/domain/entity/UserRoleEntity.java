package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * User-Role Entity - 사용자-역할 매핑
 * Maps to: common.SI_UserRoles
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_user_roles",
    schema = "common",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_si_user_roles",
            columnNames = {"user_id", "role_id"}
        )
    },
    indexes = {
        @Index(name = "idx_si_user_roles_user_id", columnList = "user_id"),
        @Index(name = "idx_si_user_roles_role_id", columnList = "role_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_role_id")
    private Long userRoleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_user_roles_user_id"))
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_user_roles_role_id"))
    private RoleEntity role;
}
