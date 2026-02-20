package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.math.BigDecimal;

/**
 * Inspection Form Field Entity
 * 점검 양식 필드 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_inspection_form_fields",
    indexes = {
        @Index(name = "idx_inspection_form_field_form", columnList = "form_id"),
        @Index(name = "idx_inspection_form_field_order", columnList = "field_order")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionFormFieldEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "field_id")
    private Long fieldId;

    // Parent Form
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private InspectionFormEntity form;

    @Column(name = "field_name", nullable = false, length = 200)
    private String fieldName;

    @Column(name = "field_type", nullable = false, length = 30)
    private String fieldType;

    @Column(name = "field_order")
    private Integer fieldOrder;

    @Column(name = "is_required")
    private Boolean isRequired;

    @Column(name = "options", columnDefinition = "TEXT")
    private String options;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "min_value", precision = 15, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 15, scale = 4)
    private BigDecimal maxValue;
}
