package kr.co.softice.mes.domain.entity;

import javax.persistence.*;
import lombok.*;

/**
 * Document Template Entity
 * 문서 양식 엔티티
 *
 * @author Moon Myung-seop
 */
@Entity
@Table(schema = "common", name = "SD_document_templates",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_sd_template_code", columnNames = {"tenant_id", "template_code", "version"})
    },
    indexes = {
        @Index(name = "idx_sd_template_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sd_template_type", columnList = "template_type"),
        @Index(name = "idx_sd_template_category", columnList = "category"),
        @Index(name = "idx_sd_template_active", columnList = "is_active"),
        @Index(name = "idx_sd_template_latest", columnList = "is_latest")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplateEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    // Tenant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private TenantEntity tenant;

    @Column(name = "template_code", nullable = false, length = 50)
    private String templateCode;

    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType; // SOP, CHECKLIST, INSPECTION_SHEET, REPORT

    @Column(name = "category", length = 50)
    private String category; // PRODUCTION, WAREHOUSE, QUALITY, FACILITY, COMMON

    // File information
    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "file_type", length = 50)
    private String fileType; // EXCEL, WORD, PDF, HTML

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "template_content", columnDefinition = "TEXT")
    private String templateContent;

    // Version management
    @Column(name = "version", length = 20)
    @Builder.Default
    private String version = "1.0";

    @Column(name = "is_latest")
    @Builder.Default
    private Boolean isLatest = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
