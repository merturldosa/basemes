package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * User Entity - 사용자
 * Maps to: common.SI_Users
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "si_users",
    schema = "common",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_si_users_tenant_username",
            columnNames = {"tenant_id", "username"}
        )
    },
    indexes = {
        @Index(name = "idx_si_users_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_si_users_email", columnList = "email"),
        @Index(name = "idx_si_users_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, foreignKey = @ForeignKey(name = "fk_si_users_tenant_id"))
    private TenantEntity tenant;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "preferred_language", length = 10)
    @Builder.Default
    private String preferredLanguage = "ko";

    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;
}
