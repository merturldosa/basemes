package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.DocumentTemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Document Template Repository
 * 문서 양식 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplateEntity, Long> {

    /**
     * Find all templates by tenant ID
     */
    @Query("SELECT dt FROM DocumentTemplateEntity dt " +
            "WHERE dt.tenant.tenantId = :tenantId " +
            "ORDER BY dt.displayOrder ASC, dt.templateName ASC")
    List<DocumentTemplateEntity> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find active templates by tenant ID
     */
    @Query("SELECT dt FROM DocumentTemplateEntity dt " +
            "WHERE dt.tenant.tenantId = :tenantId " +
            "AND dt.isActive = true " +
            "ORDER BY dt.displayOrder ASC, dt.templateName ASC")
    List<DocumentTemplateEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find templates by type
     */
    @Query("SELECT dt FROM DocumentTemplateEntity dt " +
            "WHERE dt.tenant.tenantId = :tenantId " +
            "AND dt.templateType = :templateType " +
            "AND dt.isActive = true " +
            "ORDER BY dt.displayOrder ASC, dt.templateName ASC")
    List<DocumentTemplateEntity> findByTenantIdAndTemplateType(
            @Param("tenantId") String tenantId,
            @Param("templateType") String templateType);

    /**
     * Find templates by category
     */
    @Query("SELECT dt FROM DocumentTemplateEntity dt " +
            "WHERE dt.tenant.tenantId = :tenantId " +
            "AND dt.category = :category " +
            "AND dt.isActive = true " +
            "ORDER BY dt.displayOrder ASC, dt.templateName ASC")
    List<DocumentTemplateEntity> findByTenantIdAndCategory(
            @Param("tenantId") String tenantId,
            @Param("category") String category);

    /**
     * Find template by code and tenant
     */
    @Query("SELECT dt FROM DocumentTemplateEntity dt " +
            "WHERE dt.tenant.tenantId = :tenantId " +
            "AND dt.templateCode = :templateCode " +
            "AND dt.isLatest = true")
    Optional<DocumentTemplateEntity> findLatestByTenantIdAndTemplateCode(
            @Param("tenantId") String tenantId,
            @Param("templateCode") String templateCode);

    /**
     * Find template by code and version
     */
    @Query("SELECT dt FROM DocumentTemplateEntity dt " +
            "WHERE dt.tenant.tenantId = :tenantId " +
            "AND dt.templateCode = :templateCode " +
            "AND dt.version = :version")
    Optional<DocumentTemplateEntity> findByTenantIdAndTemplateCodeAndVersion(
            @Param("tenantId") String tenantId,
            @Param("templateCode") String templateCode,
            @Param("version") String version);

    /**
     * Find all versions of a template
     */
    @Query("SELECT dt FROM DocumentTemplateEntity dt " +
            "WHERE dt.tenant.tenantId = :tenantId " +
            "AND dt.templateCode = :templateCode " +
            "ORDER BY dt.version DESC")
    List<DocumentTemplateEntity> findAllVersionsByTenantIdAndTemplateCode(
            @Param("tenantId") String tenantId,
            @Param("templateCode") String templateCode);

    /**
     * Check if template code exists
     */
    @Query("SELECT COUNT(dt) > 0 FROM DocumentTemplateEntity dt " +
            "WHERE dt.tenant.tenantId = :tenantId " +
            "AND dt.templateCode = :templateCode")
    boolean existsByTenantIdAndTemplateCode(
            @Param("tenantId") String tenantId,
            @Param("templateCode") String templateCode);
}
