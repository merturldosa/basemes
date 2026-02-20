package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Code Entity - 공통 코드
 * Maps to: common.SD_Codes
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(
    name = "sd_codes",
    schema = "common",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_sd_codes_group_code",
            columnNames = {"group_id", "code"}
        )
    },
    indexes = {
        @Index(name = "idx_sd_codes_group_id", columnList = "group_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_id")
    private Long codeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sd_codes_group_id"))
    private CodeGroupEntity codeGroup;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "description", length = 500)
    private String description;
}
