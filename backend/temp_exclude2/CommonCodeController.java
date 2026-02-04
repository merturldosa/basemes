package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.CommonCodeDetailEntity;
import kr.co.softice.mes.domain.entity.CommonCodeGroupEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Common Code Controller
 * 공통 코드 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/common-codes")
@RequiredArgsConstructor
@Tag(name = "Common Code Management", description = "공통 코드 관리 API")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;
    private final TenantRepository tenantRepository;

    // ================== Code Group APIs ==================

    /**
     * 코드 그룹 목록 조회
     * GET /api/common-codes/groups
     */
    @GetMapping("/groups")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드 그룹 목록 조회", description = "테넌트의 모든 코드 그룹 조회")
    public ResponseEntity<ApiResponse<List<CommonCodeGroupEntity>>> getCodeGroups() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting code groups for tenant: {}", tenantId);

        List<CommonCodeGroupEntity> codeGroups = commonCodeService.findAllCodeGroups(tenantId);

        return ResponseEntity.ok(ApiResponse.success("코드 그룹 목록 조회 성공", codeGroups));
    }

    /**
     * 활성 코드 그룹 목록 조회
     * GET /api/common-codes/groups/active
     */
    @GetMapping("/groups/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 코드 그룹 목록 조회", description = "활성 상태의 코드 그룹만 조회")
    public ResponseEntity<ApiResponse<List<CommonCodeGroupEntity>>> getActiveCodeGroups() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active code groups for tenant: {}", tenantId);

        List<CommonCodeGroupEntity> codeGroups = commonCodeService.findActiveCodeGroups(tenantId);

        return ResponseEntity.ok(ApiResponse.success("활성 코드 그룹 목록 조회 성공", codeGroups));
    }

    /**
     * 코드 그룹 상세 조회
     * GET /api/common-codes/groups/{id}
     */
    @GetMapping("/groups/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드 그룹 상세 조회", description = "코드 그룹 상세 정보 조회 (코드 상세 포함)")
    public ResponseEntity<ApiResponse<CommonCodeGroupEntity>> getCodeGroup(@PathVariable Long id) {
        log.info("Getting code group: {}", id);

        CommonCodeGroupEntity codeGroup = commonCodeService.findCodeGroupById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("코드 그룹 조회 성공", codeGroup));
    }

    /**
     * 코드 그룹명으로 조회
     * GET /api/common-codes/groups/by-code/{codeGroup}
     */
    @GetMapping("/groups/by-code/{codeGroup}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드 그룹명으로 조회", description = "코드 그룹명으로 상세 정보 조회")
    public ResponseEntity<ApiResponse<CommonCodeGroupEntity>> getCodeGroupByCode(
            @PathVariable String codeGroup) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting code group by code: {} for tenant: {}", codeGroup, tenantId);

        CommonCodeGroupEntity group = commonCodeService.findCodeGroupByCode(tenantId, codeGroup)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("코드 그룹 조회 성공", group));
    }

    /**
     * 코드 그룹 생성
     * POST /api/common-codes/groups
     */
    @PostMapping("/groups")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "코드 그룹 생성", description = "새로운 코드 그룹 생성")
    public ResponseEntity<ApiResponse<CommonCodeGroupEntity>> createCodeGroup(
            @RequestBody CommonCodeGroupEntity codeGroup) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating code group: {} for tenant: {}", codeGroup.getCodeGroup(), tenantId);

        // Set tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));
        codeGroup.setTenant(tenant);

        CommonCodeGroupEntity created = commonCodeService.createCodeGroup(codeGroup);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("코드 그룹 생성 성공", created));
    }

    /**
     * 코드 그룹 수정
     * PUT /api/common-codes/groups/{id}
     */
    @PutMapping("/groups/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "코드 그룹 수정", description = "코드 그룹 정보 수정")
    public ResponseEntity<ApiResponse<CommonCodeGroupEntity>> updateCodeGroup(
            @PathVariable Long id,
            @RequestBody CommonCodeGroupEntity codeGroup) {
        log.info("Updating code group: {}", id);

        codeGroup.setCodeGroupId(id);
        CommonCodeGroupEntity updated = commonCodeService.updateCodeGroup(codeGroup);

        return ResponseEntity.ok(ApiResponse.success("코드 그룹 수정 성공", updated));
    }

    /**
     * 코드 그룹 삭제
     * DELETE /api/common-codes/groups/{id}
     */
    @DeleteMapping("/groups/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "코드 그룹 삭제", description = "코드 그룹 삭제 (시스템 코드는 삭제 불가)")
    public ResponseEntity<ApiResponse<Void>> deleteCodeGroup(@PathVariable Long id) {
        log.info("Deleting code group: {}", id);

        commonCodeService.deleteCodeGroup(id);

        return ResponseEntity.ok(ApiResponse.success("코드 그룹 삭제 성공", null));
    }

    // ================== Code Detail APIs ==================

    /**
     * 코드 상세 목록 조회 (by code group ID)
     * GET /api/common-codes/groups/{groupId}/details
     */
    @GetMapping("/groups/{groupId}/details")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드 상세 목록 조회", description = "코드 그룹의 모든 코드 상세 조회")
    public ResponseEntity<ApiResponse<List<CommonCodeDetailEntity>>> getCodeDetails(
            @PathVariable Long groupId) {
        log.info("Getting code details for group: {}", groupId);

        List<CommonCodeDetailEntity> details = commonCodeService.findCodeDetailsByGroupId(groupId);

        return ResponseEntity.ok(ApiResponse.success("코드 상세 목록 조회 성공", details));
    }

    /**
     * 활성 코드 상세 목록 조회
     * GET /api/common-codes/groups/{groupId}/details/active
     */
    @GetMapping("/groups/{groupId}/details/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 코드 상세 목록 조회", description = "활성 상태의 코드 상세만 조회")
    public ResponseEntity<ApiResponse<List<CommonCodeDetailEntity>>> getActiveCodeDetails(
            @PathVariable Long groupId) {
        log.info("Getting active code details for group: {}", groupId);

        List<CommonCodeDetailEntity> details = commonCodeService.findActiveCodeDetailsByGroupId(groupId);

        return ResponseEntity.ok(ApiResponse.success("활성 코드 상세 목록 조회 성공", details));
    }

    /**
     * 코드 그룹명으로 코드 상세 목록 조회
     * GET /api/common-codes/{codeGroup}/codes
     */
    @GetMapping("/{codeGroup}/codes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드 그룹명으로 코드 목록 조회", description = "코드 그룹명으로 활성 코드 목록 조회")
    public ResponseEntity<ApiResponse<List<CommonCodeDetailEntity>>> getCodesByGroupCode(
            @PathVariable String codeGroup) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting codes for group: {} in tenant: {}", codeGroup, tenantId);

        List<CommonCodeDetailEntity> codes = commonCodeService.findCodeDetailsByCodeGroup(tenantId, codeGroup);

        return ResponseEntity.ok(ApiResponse.success("코드 목록 조회 성공", codes));
    }

    /**
     * 코드 상세 조회
     * GET /api/common-codes/details/{id}
     */
    @GetMapping("/details/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드 상세 조회", description = "코드 상세 정보 조회")
    public ResponseEntity<ApiResponse<CommonCodeDetailEntity>> getCodeDetail(@PathVariable Long id) {
        log.info("Getting code detail: {}", id);

        CommonCodeDetailEntity detail = commonCodeService.findCodeDetailById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("코드 상세 조회 성공", detail));
    }

    /**
     * 코드 상세 생성
     * POST /api/common-codes/groups/{groupId}/details
     */
    @PostMapping("/groups/{groupId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "코드 상세 생성", description = "새로운 코드 상세 생성")
    public ResponseEntity<ApiResponse<CommonCodeDetailEntity>> createCodeDetail(
            @PathVariable Long groupId,
            @RequestBody CommonCodeDetailEntity codeDetail) {
        log.info("Creating code detail in group: {}", groupId);

        // Set code group
        CommonCodeGroupEntity codeGroup = commonCodeService.findCodeGroupById(groupId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));
        codeDetail.setCodeGroup(codeGroup);

        CommonCodeDetailEntity created = commonCodeService.createCodeDetail(codeDetail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("코드 상세 생성 성공", created));
    }

    /**
     * 코드 상세 수정
     * PUT /api/common-codes/details/{id}
     */
    @PutMapping("/details/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "코드 상세 수정", description = "코드 상세 정보 수정")
    public ResponseEntity<ApiResponse<CommonCodeDetailEntity>> updateCodeDetail(
            @PathVariable Long id,
            @RequestBody CommonCodeDetailEntity codeDetail) {
        log.info("Updating code detail: {}", id);

        codeDetail.setCodeDetailId(id);
        CommonCodeDetailEntity updated = commonCodeService.updateCodeDetail(codeDetail);

        return ResponseEntity.ok(ApiResponse.success("코드 상세 수정 성공", updated));
    }

    /**
     * 코드 상세 삭제
     * DELETE /api/common-codes/details/{id}
     */
    @DeleteMapping("/details/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_ADMIN')")
    @Operation(summary = "코드 상세 삭제", description = "코드 상세 삭제")
    public ResponseEntity<ApiResponse<Void>> deleteCodeDetail(@PathVariable Long id) {
        log.info("Deleting code detail: {}", id);

        commonCodeService.deleteCodeDetail(id);

        return ResponseEntity.ok(ApiResponse.success("코드 상세 삭제 성공", null));
    }

    // ================== Utility APIs ==================

    /**
     * 전체 코드 맵 조회
     * GET /api/common-codes/all
     */
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "전체 코드 맵 조회", description = "모든 코드를 Map 형태로 조회")
    public ResponseEntity<ApiResponse<Map<String, List<CommonCodeDetailEntity>>>> getAllCodes() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all codes as map for tenant: {}", tenantId);

        Map<String, List<CommonCodeDetailEntity>> codesMap = commonCodeService.getAllCodesAsMap(tenantId);

        return ResponseEntity.ok(ApiResponse.success("전체 코드 맵 조회 성공", codesMap));
    }

    /**
     * 코드명 조회
     * GET /api/common-codes/{codeGroup}/{code}/name
     */
    @GetMapping("/{codeGroup}/{code}/name")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드명 조회", description = "코드 그룹과 코드로 코드명 조회")
    public ResponseEntity<ApiResponse<String>> getCodeName(
            @PathVariable String codeGroup,
            @PathVariable String code) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting code name for: {}.{} in tenant: {}", codeGroup, code, tenantId);

        String codeName = commonCodeService.getCodeName(tenantId, codeGroup, code);

        return ResponseEntity.ok(ApiResponse.success("코드명 조회 성공", codeName));
    }

    /**
     * 코드 유효성 검증
     * GET /api/common-codes/{codeGroup}/{code}/validate
     */
    @GetMapping("/{codeGroup}/{code}/validate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "코드 유효성 검증", description = "코드가 존재하고 활성 상태인지 검증")
    public ResponseEntity<ApiResponse<Boolean>> validateCode(
            @PathVariable String codeGroup,
            @PathVariable String code) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Validating code: {}.{} in tenant: {}", codeGroup, code, tenantId);

        boolean isValid = commonCodeService.validateCode(tenantId, codeGroup, code);

        return ResponseEntity.ok(ApiResponse.success("코드 유효성 검증 완료", isValid));
    }
}
