package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Inspection Form Entity
 * 점검 양식 엔티티
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "equipment", name = "sd_inspection_forms",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_inspection_form_code", columnNames = {"tenant_id", "form_code"})
    },
    indexes = {
        @Index(name = "idx_inspection_form_tenant", columnList = "tenant_id"),
        @Index(name = "idx_inspection_form_type", columnList = "equipment_type"),
        @Index(name = "idx_inspection_form_insp_type", columnList = "inspection_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspectionFormEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "form_id")
    private Long formId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "form_code", nullable = false, length = 50)
    private String formCode;

    @Column(name = "form_name", nullable = false, length = 200)
    private String formName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "equipment_type", length = 30)
    private String equipmentType;

    @Column(name = "inspection_type", length = 30)
    private String inspectionType;

    @Column(name = "is_active")
    private Boolean isActive;

    // Form Fields
    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InspectionFormFieldEntity> fields = new ArrayList<>();
}
