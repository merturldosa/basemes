package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.CommonCodeDetailEntity;
import kr.co.softice.mes.domain.entity.CommonCodeGroupEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.CommonCodeDetailRepository;
import kr.co.softice.mes.domain.repository.CommonCodeGroupRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Common Code Controller
 * 공통 코드 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
@Tag(name = "CommonCode", description = "공통 코드 관리 API")
public class CommonCodeController {

    private final CommonCodeGroupRepository codeGroupRepository;
    private final CommonCodeDetailRepository codeDetailRepository;
    private final TenantRepository tenantRepository;

    // ==================== Code Group ====================

    @GetMapping("/groups")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "코드 그룹 목록 조회", description = "모든 공통 코드 그룹을 조회합니다.")
    public ResponseEntity<List<CommonCodeGroupEntity>> getAllCodeGroups() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all code groups for tenant: {}", tenantId);
        List<CommonCodeGroupEntity> groups = codeGroupRepository.findByTenantIdWithAllRelations(tenantId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/groups/active")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "활성 코드 그룹 목록 조회", description = "활성 상태의 코드 그룹을 조회합니다.")
    public ResponseEntity<List<CommonCodeGroupEntity>> getActiveCodeGroups() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active code groups for tenant: {}", tenantId);
        List<CommonCodeGroupEntity> groups = codeGroupRepository.findByTenant_TenantIdAndIsActiveTrue(tenantId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/groups/{codeGroupId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "코드 그룹 상세 조회", description = "ID로 코드 그룹을 조회합니다.")
    public ResponseEntity<CommonCodeGroupEntity> getCodeGroupById(@PathVariable Long codeGroupId) {
        log.info("Getting code group by ID: {}", codeGroupId);
        CommonCodeGroupEntity group = codeGroupRepository.findByIdWithAllRelations(codeGroupId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CODE_GROUP_NOT_FOUND));
        return ResponseEntity.ok(group);
    }

    @GetMapping("/groups/by-code/{codeGroup}")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "코드 그룹명으로 조회", description = "코드 그룹명으로 그룹과 상세 코드를 조회합니다.")
    public ResponseEntity<CommonCodeGroupEntity> getCodeGroupByCode(@PathVariable String codeGroup) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting code group by code: {} for tenant: {}", codeGroup, tenantId);
        CommonCodeGroupEntity group = codeGroupRepository.findByTenantIdAndCodeGroupWithDetails(tenantId, codeGroup)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CODE_GROUP_NOT_FOUND));
        return ResponseEntity.ok(group);
    }

    @PostMapping("/groups")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Transactional
    @Operation(summary = "코드 그룹 등록", description = "새로운 코드 그룹을 등록합니다.")
    public ResponseEntity<CommonCodeGroupEntity> createCodeGroup(@RequestBody CommonCodeGroupEntity group) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating code group: {} for tenant: {}", group.getCodeGroup(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        if (codeGroupRepository.existsByTenantAndCodeGroup(tenant, group.getCodeGroup())) {
            throw new BusinessException(ErrorCode.CODE_GROUP_ALREADY_EXISTS);
        }

        group.setTenant(tenant);
        CommonCodeGroupEntity saved = codeGroupRepository.save(group);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/groups/{codeGroupId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Transactional
    @Operation(summary = "코드 그룹 수정", description = "코드 그룹 정보를 수정합니다.")
    public ResponseEntity<CommonCodeGroupEntity> updateCodeGroup(
            @PathVariable Long codeGroupId,
            @RequestBody CommonCodeGroupEntity group) {
        log.info("Updating code group ID: {}", codeGroupId);

        CommonCodeGroupEntity existing = codeGroupRepository.findById(codeGroupId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CODE_GROUP_NOT_FOUND));

        existing.setCodeGroupName(group.getCodeGroupName());
        existing.setDescription(group.getDescription());
        existing.setDisplayOrder(group.getDisplayOrder());
        existing.setIsActive(group.getIsActive());

        CommonCodeGroupEntity updated = codeGroupRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/groups/{codeGroupId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "코드 그룹 삭제", description = "코드 그룹을 삭제합니다.")
    public ResponseEntity<Void> deleteCodeGroup(@PathVariable Long codeGroupId) {
        log.info("Deleting code group ID: {}", codeGroupId);

        CommonCodeGroupEntity group = codeGroupRepository.findById(codeGroupId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CODE_GROUP_NOT_FOUND));

        if (group.getIsSystem()) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION, "시스템 코드 그룹은 삭제할 수 없습니다.");
        }

        codeGroupRepository.delete(group);
        return ResponseEntity.ok().build();
    }

    // ==================== Code Detail ====================

    @GetMapping("/groups/{codeGroupId}/details")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "코드 상세 목록 조회", description = "코드 그룹의 상세 코드를 조회합니다.")
    public ResponseEntity<List<CommonCodeDetailEntity>> getCodeDetails(@PathVariable Long codeGroupId) {
        log.info("Getting code details for group ID: {}", codeGroupId);
        List<CommonCodeDetailEntity> details = codeDetailRepository
                .findByCodeGroupIdWithAllRelations(codeGroupId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/groups/{codeGroupId}/details/active")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "활성 코드 상세 목록 조회", description = "코드 그룹의 활성 상세 코드를 조회합니다.")
    public ResponseEntity<List<CommonCodeDetailEntity>> getActiveCodeDetails(@PathVariable Long codeGroupId) {
        log.info("Getting active code details for group ID: {}", codeGroupId);
        List<CommonCodeDetailEntity> details = codeDetailRepository
                .findActiveCodesByCodeGroupIdOrdered(codeGroupId);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/lookup/{codeGroup}")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    @Operation(summary = "코드 조회 (그룹명)", description = "그룹명으로 활성 상세 코드를 조회합니다.")
    public ResponseEntity<List<CommonCodeDetailEntity>> lookupCodes(@PathVariable String codeGroup) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Looking up codes for group: {} in tenant: {}", codeGroup, tenantId);
        List<CommonCodeDetailEntity> details = codeDetailRepository
                .findByTenantIdAndCodeGroup(tenantId, codeGroup);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/groups/{codeGroupId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Transactional
    @Operation(summary = "코드 상세 등록", description = "코드 그룹에 상세 코드를 등록합니다.")
    public ResponseEntity<CommonCodeDetailEntity> createCodeDetail(
            @PathVariable Long codeGroupId,
            @RequestBody CommonCodeDetailEntity detail) {
        log.info("Creating code detail for group ID: {}", codeGroupId);

        CommonCodeGroupEntity group = codeGroupRepository.findById(codeGroupId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CODE_GROUP_NOT_FOUND));

        if (codeDetailRepository.existsByCodeGroupAndCode(group, detail.getCode())) {
            throw new BusinessException(ErrorCode.CODE_ALREADY_EXISTS);
        }

        detail.setCodeGroup(group);
        CommonCodeDetailEntity saved = codeDetailRepository.save(detail);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/details/{codeDetailId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Transactional
    @Operation(summary = "코드 상세 수정", description = "상세 코드를 수정합니다.")
    public ResponseEntity<CommonCodeDetailEntity> updateCodeDetail(
            @PathVariable Long codeDetailId,
            @RequestBody CommonCodeDetailEntity detail) {
        log.info("Updating code detail ID: {}", codeDetailId);

        CommonCodeDetailEntity existing = codeDetailRepository.findById(codeDetailId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CODE_NOT_FOUND));

        existing.setCodeName(detail.getCodeName());
        existing.setDescription(detail.getDescription());
        existing.setDisplayOrder(detail.getDisplayOrder());
        existing.setIsDefault(detail.getIsDefault());
        existing.setIsActive(detail.getIsActive());

        CommonCodeDetailEntity updated = codeDetailRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/details/{codeDetailId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "코드 상세 삭제", description = "상세 코드를 삭제합니다.")
    public ResponseEntity<Void> deleteCodeDetail(@PathVariable Long codeDetailId) {
        log.info("Deleting code detail ID: {}", codeDetailId);

        CommonCodeDetailEntity detail = codeDetailRepository.findById(codeDetailId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.CODE_NOT_FOUND));

        codeDetailRepository.delete(detail);
        return ResponseEntity.ok().build();
    }
}
