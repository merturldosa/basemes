package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * SOP Step Entity
 * SOP 실행 단계 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "sop_steps",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sop_step", columnNames = {"sop_id", "step_number"})
    },
    indexes = {
        @Index(name = "idx_sop_step_sop", columnList = "sop_id"),
        @Index(name = "idx_sop_step_number", columnList = "step_number"),
        @Index(name = "idx_sop_step_critical", columnList = "is_critical")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SOPStepEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sop_step_id")
    private Long sopStepId;

    // Parent SOP
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sop_id", nullable = false)
    private SOPEntity sop;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(name = "step_title", nullable = false, length = 200)
    private String stepTitle;

    @Column(name = "step_description", columnDefinition = "TEXT")
    private String stepDescription;

    // Step details
    @Column(name = "step_type", length = 50)
    private String stepType; // PREPARATION, EXECUTION, INSPECTION, DOCUMENTATION, SAFETY

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // Minutes

    // Instructions
    @Column(name = "detailed_instruction", columnDefinition = "TEXT")
    private String detailedInstruction;

    @Column(name = "caution_notes", columnDefinition = "TEXT")
    private String cautionNotes;

    @Column(name = "quality_points", columnDefinition = "TEXT")
    private String qualityPoints;

    // Media
    @Column(name = "image_urls", columnDefinition = "JSONB")
    private String imageUrls; // JSON array of image URLs

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    // Checklist items
    @Column(name = "checklist_items", columnDefinition = "JSONB")
    private String checklistItems; // JSON array of checklist items

    // Dependencies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prerequisite_step_id")
    private SOPStepEntity prerequisiteStep; // Must complete this step first

    @Column(name = "is_critical")
    @Builder.Default
    private Boolean isCritical = false;

    @Column(name = "is_mandatory")
    @Builder.Default
    private Boolean isMandatory = true;

    // Execution results (One-to-Many)
    @OneToMany(mappedBy = "sopStep", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SOPExecutionStepEntity> executionResults = new ArrayList<>();

    /**
     * Check if step can be started
     * (prerequisite step must be completed)
     */
    public boolean canStart(SOPExecutionEntity execution) {
        if (prerequisiteStep == null) {
            return true;
        }

        // Find execution result for prerequisite step
        return execution.getExecutionSteps().stream()
                .anyMatch(es -> es.getSopStep().equals(prerequisiteStep)
                        && "COMPLETED".equals(es.getStepStatus()));
    }

    /**
     * Check if step is skippable
     */
    public boolean isSkippable() {
        return !isMandatory && !isCritical;
    }
}
