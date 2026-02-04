package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.MaterialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Material Repository
 * 자재 마스터 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {

    /**
     * 테넌트별 모든 자재 조회 (JOIN FETCH)
     */
    @Query("SELECT m FROM MaterialEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.supplier s " +
           "LEFT JOIN FETCH s.tenant " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "ORDER BY m.materialCode ASC")
    List<MaterialEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * 자재 ID로 조회 (JOIN FETCH)
     */
    @Query("SELECT m FROM MaterialEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.supplier s " +
           "LEFT JOIN FETCH s.tenant " +
           "WHERE m.materialId = :materialId")
    Optional<MaterialEntity> findByIdWithAllRelations(@Param("materialId") Long materialId);

    /**
     * 테넌트별 활성 자재 조회
     */
    @Query("SELECT m FROM MaterialEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.supplier s " +
           "LEFT JOIN FETCH s.tenant " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "AND m.isActive = true " +
           "ORDER BY m.materialCode ASC")
    List<MaterialEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * 테넌트 및 자재 유형별 조회
     */
    @Query("SELECT m FROM MaterialEntity m " +
           "JOIN FETCH m.tenant " +
           "LEFT JOIN FETCH m.supplier s " +
           "LEFT JOIN FETCH s.tenant " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "AND m.materialType = :materialType " +
           "ORDER BY m.materialCode ASC")
    List<MaterialEntity> findByTenantIdAndType(
        @Param("tenantId") String tenantId,
        @Param("materialType") String materialType
    );

    /**
     * 테넌트 및 공급업체별 조회
     */
    @Query("SELECT m FROM MaterialEntity m " +
           "JOIN FETCH m.tenant " +
           "JOIN FETCH m.supplier s " +
           "LEFT JOIN FETCH s.tenant " +
           "WHERE m.tenant.tenantId = :tenantId " +
           "AND m.supplier.supplierId = :supplierId " +
           "ORDER BY m.materialCode ASC")
    List<MaterialEntity> findByTenantIdAndSupplierId(
        @Param("tenantId") String tenantId,
        @Param("supplierId") Long supplierId
    );

    /**
     * 테넌트 및 자재 코드로 존재 여부 확인
     */
    boolean existsByTenant_TenantIdAndMaterialCode(String tenantId, String materialCode);

    /**
     * 테넌트 및 자재 코드로 조회
     */
    Optional<MaterialEntity> findByTenant_TenantIdAndMaterialCode(String tenantId, String materialCode);
}
