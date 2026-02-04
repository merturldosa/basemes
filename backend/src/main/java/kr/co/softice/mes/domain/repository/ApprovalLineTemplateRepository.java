package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ApprovalLineTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Approval Line Template Repository
 * 결재 라인 템플릿 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ApprovalLineTemplateRepository extends JpaRepository<ApprovalLineTemplateEntity, Long> {

    /**
     * Find all templates by tenant ID
     */
    @Query("SELECT t FROM ApprovalLineTemplateEntity t " +
            "JOIN FETCH t.tenant " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "ORDER BY t.templateName ASC")
    List<ApprovalLineTemplateEntity> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find active templates by tenant ID
     */
    @Query("SELECT t FROM ApprovalLineTemplateEntity t " +
            "JOIN FETCH t.tenant " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.isActive = true " +
            "ORDER BY t.templateName ASC")
    List<ApprovalLineTemplateEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find template by ID with steps
     */
    @Query("SELECT t FROM ApprovalLineTemplateEntity t " +
            "LEFT JOIN FETCH t.steps " +
            "WHERE t.templateId = :templateId")
    Optional<ApprovalLineTemplateEntity> findByIdWithSteps(@Param("templateId") Long templateId);

    /**
     * Find template by code
     */
    @Query("SELECT t FROM ApprovalLineTemplateEntity t " +
            "JOIN FETCH t.tenant " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.templateCode = :templateCode")
    Optional<ApprovalLineTemplateEntity> findByTenantIdAndTemplateCode(
            @Param("tenantId") String tenantId,
            @Param("templateCode") String templateCode);

    /**
     * Find templates by document type
     */
    @Query("SELECT t FROM ApprovalLineTemplateEntity t " +
            "JOIN FETCH t.tenant " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.documentType = :documentType " +
            "AND t.isActive = true " +
            "ORDER BY t.isDefault DESC, t.templateName ASC")
    List<ApprovalLineTemplateEntity> findByTenantIdAndDocumentType(
            @Param("tenantId") String tenantId,
            @Param("documentType") String documentType);

    /**
     * Find default template for document type
     */
    @Query("SELECT t FROM ApprovalLineTemplateEntity t " +
            "LEFT JOIN FETCH t.steps " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.documentType = :documentType " +
            "AND t.isDefault = true " +
            "AND t.isActive = true")
    Optional<ApprovalLineTemplateEntity> findDefaultByTenantIdAndDocumentType(
            @Param("tenantId") String tenantId,
            @Param("documentType") String documentType);

    /**
     * Check if template code exists
     */
    @Query("SELECT COUNT(t) > 0 FROM ApprovalLineTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.templateCode = :templateCode")
    boolean existsByTenantIdAndTemplateCode(
            @Param("tenantId") String tenantId,
            @Param("templateCode") String templateCode);

    /**
     * Find templates by approval type
     */
    @Query("SELECT t FROM ApprovalLineTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.approvalType = :approvalType " +
            "AND t.isActive = true " +
            "ORDER BY t.templateName ASC")
    List<ApprovalLineTemplateEntity> findByTenantIdAndApprovalType(
            @Param("tenantId") String tenantId,
            @Param("approvalType") String approvalType);

    /**
     * Count templates by document type
     */
    @Query("SELECT COUNT(t) FROM ApprovalLineTemplateEntity t " +
            "WHERE t.tenant.tenantId = :tenantId " +
            "AND t.documentType = :documentType " +
            "AND t.isActive = true")
    Long countByTenantIdAndDocumentType(
            @Param("tenantId") String tenantId,
            @Param("documentType") String documentType);
}
