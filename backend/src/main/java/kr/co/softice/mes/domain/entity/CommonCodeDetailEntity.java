package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Common Code Detail Entity
 * 공통 코드 상세 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_common_code_details",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_code_detail", columnNames = {"code_group_id", "code"})
    },
    indexes = {
        @Index(name = "idx_sd_code_detail_group", columnList = "code_group_id"),
        @Index(name = "idx_sd_code_detail_active", columnList = "is_active"),
        @Index(name = "idx_sd_code_detail_order", columnList = "display_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonCodeDetailEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_detail_id")
    private Long codeDetailId;

    // Code Group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_group_id", nullable = false)
    private CommonCodeGroupEntity codeGroup;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Extended fields for flexible use
    @Column(name = "value1", length = 255)
    private String value1;

    @Column(name = "value2", length = 255)
    private String value2;

    @Column(name = "value3", length = 255)
    private String value3;

    @Column(name = "value4", length = 255)
    private String value4;

    @Column(name = "value5", length = 255)
    private String value5;

    // UI fields
    @Column(name = "color_code", length = 20)
    private String colorCode;

    @Column(name = "icon_name", length = 50)
    private String iconName;
}
