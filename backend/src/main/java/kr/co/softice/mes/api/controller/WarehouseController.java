package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.inventory.WarehouseCreateRequest;
import kr.co.softice.mes.common.dto.inventory.WarehouseResponse;
import kr.co.softice.mes.common.dto.inventory.WarehouseUpdateRequest;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.entity.WarehouseEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import kr.co.softice.mes.domain.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Warehouse Controller
 * 창고 관리 API
 *
 * 엔드포인트:
 * - GET /api/warehouses - 창고 목록 조회
 * - GET /api/warehouses/{id} - 창고 상세 조회
 * - GET /api/warehouses/type/{type} - 타입별 창고 조회
 * - POST /api/warehouses - 창고 생성 (WAREHOUSE_MANAGER)
 * - PUT /api/warehouses/{id} - 창고 수정
 * - DELETE /api/warehouses/{id} - 창고 비활성화
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
@Tag(name = "Warehouse Management", description = "창고 관리 API")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    /**
     * 창고 목록 조회
     * GET /api/warehouses
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "창고 목록 조회", description = "테넌트의 모든 창고 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getWarehouses(
            @RequestParam(required = false) Boolean activeOnly) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting warehouses for tenant: {}, activeOnly: {}", tenantId, activeOnly);

        List<WarehouseEntity> warehouses;
        if (Boolean.TRUE.equals(activeOnly)) {
            warehouses = warehouseService.findActiveByTenant(tenantId);
        } else {
            warehouses = warehouseService.findByTenant(tenantId);
        }

        List<WarehouseResponse> responses = warehouses.stream()
                .map(this::toWarehouseResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("창고 목록 조회 성공", responses));
    }

    /**
     * 창고 상세 조회
     * GET /api/warehouses/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "창고 상세 조회", description = "창고 ID로 상세 정보 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouse(@PathVariable Long id) {
        log.info("Getting warehouse: {}", id);

        WarehouseEntity warehouse = warehouseService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("창고 조회 성공", toWarehouseResponse(warehouse)));
    }

    /**
     * 타입별 창고 조회
     * GET /api/warehouses/type/{type}
     *
     * 창고 타입:
     * - RAW_MATERIAL: 원자재 창고
     * - WORK_IN_PROCESS (WIP): 재공품 창고
     * - FINISHED_GOODS: 완제품 창고
     * - QUARANTINE: 격리 창고
     * - SCRAP: 스크랩 창고
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "타입별 창고 조회", description = "창고 타입별 목록 조회 (RAW_MATERIAL, WIP, FINISHED_GOODS, QUARANTINE, SCRAP)")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getWarehousesByType(@PathVariable String type) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting warehouses by type: {} for tenant: {}", type, tenantId);

        // Validate warehouse type
        if (!isValidWarehouseType(type)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid warehouse type: " + type);
        }

        List<WarehouseEntity> warehouses = warehouseService.findByTenant(tenantId).stream()
                .filter(w -> type.equals(w.getWarehouseType()))
                .collect(Collectors.toList());

        List<WarehouseResponse> responses = warehouses.stream()
                .map(this::toWarehouseResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("타입별 창고 조회 성공", responses));
    }

    /**
     * 창고 생성
     * POST /api/warehouses
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "창고 생성", description = "신규 창고 등록 (WAREHOUSE_MANAGER 권한 필요)")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody WarehouseCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating warehouse: {} for tenant: {}", request.getWarehouseCode(), tenantId);

        // Validate tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        // Validate manager user
        UserEntity manager = null;
        if (request.getManagerUserId() != null) {
            manager = userRepository.findById(request.getManagerUserId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
        }

        // Validate warehouse type
        if (!isValidWarehouseType(request.getWarehouseType())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid warehouse type: " + request.getWarehouseType());
        }

        // Build warehouse entity
        WarehouseEntity warehouse = WarehouseEntity.builder()
                .tenant(tenant)
                .warehouseCode(request.getWarehouseCode())
                .warehouseName(request.getWarehouseName())
                .warehouseType(request.getWarehouseType())
                .location(request.getLocation())
                .manager(manager)
                .totalCapacity(request.getCapacity() != null ? BigDecimal.valueOf(request.getCapacity()) : null)
                .capacityUnit(request.getUnit())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .remarks(request.getRemarks())
                .build();

        WarehouseEntity created = warehouseService.createWarehouse(warehouse);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("창고 생성 성공", toWarehouseResponse(created)));
    }

    /**
     * 창고 수정
     * PUT /api/warehouses/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "창고 수정", description = "기존 창고 정보 수정")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseUpdateRequest request) {

        log.info("Updating warehouse: {}", id);

        // Find existing warehouse
        WarehouseEntity existing = warehouseService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        // Update fields
        if (request.getWarehouseName() != null) {
            existing.setWarehouseName(request.getWarehouseName());
        }
        if (request.getWarehouseType() != null) {
            if (!isValidWarehouseType(request.getWarehouseType())) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid warehouse type: " + request.getWarehouseType());
            }
            existing.setWarehouseType(request.getWarehouseType());
        }
        if (request.getLocation() != null) {
            existing.setLocation(request.getLocation());
        }
        if (request.getManagerUserId() != null) {
            UserEntity manager = userRepository.findById(request.getManagerUserId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
            existing.setManager(manager);
        }
        if (request.getCapacity() != null) {
            existing.setTotalCapacity(BigDecimal.valueOf(request.getCapacity()));
        }
        if (request.getUnit() != null) {
            existing.setCapacityUnit(request.getUnit());
        }
        if (request.getIsActive() != null) {
            existing.setIsActive(request.getIsActive());
        }
        if (request.getRemarks() != null) {
            existing.setRemarks(request.getRemarks());
        }

        WarehouseEntity updated = warehouseService.updateWarehouse(existing);

        return ResponseEntity.ok(ApiResponse.success("창고 수정 성공", toWarehouseResponse(updated)));
    }

    /**
     * 창고 비활성화 (Soft Delete)
     * DELETE /api/warehouses/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "창고 비활성화", description = "창고를 비활성화 (소프트 삭제)")
    public ResponseEntity<ApiResponse<WarehouseResponse>> deleteWarehouse(@PathVariable Long id) {
        log.info("Deactivating warehouse: {}", id);

        WarehouseEntity warehouse = warehouseService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        warehouse.setIsActive(false);
        WarehouseEntity deactivated = warehouseService.updateWarehouse(warehouse);

        return ResponseEntity.ok(ApiResponse.success("창고 비활성화 성공", toWarehouseResponse(deactivated)));
    }

    /**
     * 창고 활성/비활성 토글
     * PATCH /api/warehouses/{id}/toggle-active
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "창고 활성/비활성 토글", description = "창고 활성 상태 전환")
    public ResponseEntity<ApiResponse<WarehouseResponse>> toggleActive(@PathVariable Long id) {
        log.info("Toggling active status for warehouse: {}", id);

        WarehouseEntity toggled = warehouseService.toggleActive(id);

        return ResponseEntity.ok(ApiResponse.success("창고 상태 변경 성공", toWarehouseResponse(toggled)));
    }

    // ================== Private Helper Methods ==================

    /**
     * Convert entity to response DTO
     */
    private WarehouseResponse toWarehouseResponse(WarehouseEntity entity) {
        return WarehouseResponse.builder()
                .warehouseId(entity.getWarehouseId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .warehouseCode(entity.getWarehouseCode())
                .warehouseName(entity.getWarehouseName())
                .warehouseType(entity.getWarehouseType())
                .location(entity.getLocation())
                .managerUserId(entity.getManager() != null ? entity.getManager().getUserId() : null)
                .managerUserName(entity.getManager() != null ? entity.getManager().getUsername() : null)
                .capacity(entity.getTotalCapacity() != null ? entity.getTotalCapacity().intValue() : null)
                .unit(entity.getCapacityUnit())
                .isActive(entity.getIsActive())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Validate warehouse type
     */
    private boolean isValidWarehouseType(String type) {
        return type != null && (
                "RAW_MATERIAL".equals(type) ||
                "WORK_IN_PROCESS".equals(type) ||
                "WIP".equals(type) ||
                "FINISHED_GOODS".equals(type) ||
                "QUARANTINE".equals(type) ||
                "SCRAP".equals(type)
        );
    }
}
