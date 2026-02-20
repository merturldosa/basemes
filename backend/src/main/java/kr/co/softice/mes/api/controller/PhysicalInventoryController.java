package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.wms.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.PhysicalInventoryEntity;
import kr.co.softice.mes.domain.entity.PhysicalInventoryItemEntity;
import kr.co.softice.mes.domain.service.PhysicalInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Physical Inventory Controller
 * 실사 관리 컨트롤러
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/physical-inventories")
@RequiredArgsConstructor
@Tag(name = "Physical Inventory", description = "실사 관리 API")
public class PhysicalInventoryController {

    private final PhysicalInventoryService physicalInventoryService;

    /**
     * 실사 계획 생성
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "실사 계획 생성", description = "창고의 현재 재고를 기준으로 실사 계획 자동 생성")
    public ResponseEntity<ApiResponse<PhysicalInventoryResponse>> createPhysicalInventory(
            @Valid @RequestBody PhysicalInventoryCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Physical inventory creation request - Tenant: {}, Warehouse: {}",
                tenantId, request.getWarehouseId());

        PhysicalInventoryEntity physicalInventory = physicalInventoryService.createPhysicalInventory(
                tenantId,
                request.getWarehouseId(),
                request.getInventoryDate(),
                request.getPlannedByUserId(),
                request.getRemarks()
        );

        PhysicalInventoryResponse response = toResponse(physicalInventory);

        return ResponseEntity.ok(
                ApiResponse.success("실사 계획이 생성되었습니다", response)
        );
    }

    /**
     * 실사 목록 조회
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "실사 목록 조회", description = "테넌트의 모든 실사 계획 조회")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<PhysicalInventoryResponse>>> getPhysicalInventories() {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Physical inventory list request - Tenant: {}", tenantId);

        List<PhysicalInventoryEntity> inventories = physicalInventoryService.getPhysicalInventories(tenantId);

        List<PhysicalInventoryResponse> responses = inventories.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(String.format("실사 계획 %d건 조회 완료", responses.size()), responses)
        );
    }

    /**
     * 실사 상세 조회
     */
    @GetMapping("/{physicalInventoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "실사 상세 조회", description = "실사 계획 상세 정보 및 항목 조회")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PhysicalInventoryResponse>> getPhysicalInventory(
            @PathVariable Long physicalInventoryId) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Physical inventory detail request - Tenant: {}, ID: {}", tenantId, physicalInventoryId);

        PhysicalInventoryEntity physicalInventory = physicalInventoryService.getPhysicalInventory(
                tenantId,
                physicalInventoryId
        );

        PhysicalInventoryResponse response = toResponse(physicalInventory);

        return ResponseEntity.ok(
                ApiResponse.success("실사 계획 조회 완료", response)
        );
    }

    /**
     * 실사 수량 입력
     */
    @PostMapping("/{physicalInventoryId}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_OPERATOR')")
    @Operation(summary = "실사 수량 입력", description = "실사 항목의 실제 수량 입력")
    public ResponseEntity<ApiResponse<PhysicalInventoryResponse>> updateCountedQuantity(
            @PathVariable Long physicalInventoryId,
            @Valid @RequestBody PhysicalInventoryCountRequest request) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Counted quantity update request - Tenant: {}, Physical Inventory: {}, Item: {}",
                tenantId, physicalInventoryId, request.getItemId());

        PhysicalInventoryEntity physicalInventory = physicalInventoryService.updateCountedQuantity(
                tenantId,
                physicalInventoryId,
                request.getItemId(),
                request.getCountedQuantity(),
                request.getCountedByUserId()
        );

        PhysicalInventoryResponse response = toResponse(physicalInventory);

        return ResponseEntity.ok(
                ApiResponse.success("실사 수량이 입력되었습니다", response)
        );
    }

    /**
     * 실사 완료
     */
    @PostMapping("/{physicalInventoryId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "실사 완료", description = "실사 완료 처리")
    public ResponseEntity<ApiResponse<PhysicalInventoryResponse>> completePhysicalInventory(
            @PathVariable Long physicalInventoryId) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Physical inventory completion request - Tenant: {}, ID: {}",
                tenantId, physicalInventoryId);

        PhysicalInventoryEntity physicalInventory = physicalInventoryService.completePhysicalInventory(
                tenantId,
                physicalInventoryId
        );

        PhysicalInventoryResponse response = toResponse(physicalInventory);

        return ResponseEntity.ok(
                ApiResponse.success("실사가 완료되었습니다", response)
        );
    }

    /**
     * 재고 조정 승인
     */
    @PostMapping("/{physicalInventoryId}/items/{itemId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "재고 조정 승인", description = "실사 차이에 대한 재고 조정 승인")
    public ResponseEntity<ApiResponse<PhysicalInventoryResponse>> approveAdjustment(
            @PathVariable Long physicalInventoryId,
            @PathVariable Long itemId,
            @RequestParam Long approverId) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Adjustment approval request - Tenant: {}, Physical Inventory: {}, Item: {}, Approver: {}",
                tenantId, physicalInventoryId, itemId, approverId);

        PhysicalInventoryEntity physicalInventory = physicalInventoryService.approveAdjustment(
                tenantId,
                physicalInventoryId,
                itemId,
                approverId
        );

        PhysicalInventoryResponse response = toResponse(physicalInventory);

        return ResponseEntity.ok(
                ApiResponse.success("재고 조정이 승인되었습니다", response)
        );
    }

    /**
     * 재고 조정 거부
     */
    @PostMapping("/{physicalInventoryId}/items/{itemId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_MANAGER')")
    @Operation(summary = "재고 조정 거부", description = "실사 차이에 대한 재고 조정 거부")
    public ResponseEntity<ApiResponse<PhysicalInventoryResponse>> rejectAdjustment(
            @PathVariable Long physicalInventoryId,
            @PathVariable Long itemId,
            @RequestParam Long approverId,
            @RequestParam String reason) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Adjustment rejection request - Tenant: {}, Physical Inventory: {}, Item: {}, Reason: {}",
                tenantId, physicalInventoryId, itemId, reason);

        PhysicalInventoryEntity physicalInventory = physicalInventoryService.rejectAdjustment(
                tenantId,
                physicalInventoryId,
                itemId,
                approverId,
                reason
        );

        PhysicalInventoryResponse response = toResponse(physicalInventory);

        return ResponseEntity.ok(
                ApiResponse.success("재고 조정이 거부되었습니다", response)
        );
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private PhysicalInventoryResponse toResponse(PhysicalInventoryEntity entity) {
        // 통계 계산
        int totalItems = entity.getItems().size();
        long countedItems = entity.getItems().stream()
                .filter(item -> item.getCountedQuantity() != null)
                .count();
        long itemsRequiringAdjustment = entity.getItems().stream()
                .filter(PhysicalInventoryItemEntity::isAdjustmentRequired)
                .count();
        long approvedAdjustments = entity.getItems().stream()
                .filter(item -> PhysicalInventoryItemEntity.AdjustmentStatus.APPROVED.name()
                        .equals(item.getAdjustmentStatus()))
                .count();
        long rejectedAdjustments = entity.getItems().stream()
                .filter(item -> PhysicalInventoryItemEntity.AdjustmentStatus.REJECTED.name()
                        .equals(item.getAdjustmentStatus()))
                .count();

        PhysicalInventoryResponse.Statistics statistics = PhysicalInventoryResponse.Statistics.builder()
                .totalItems(totalItems)
                .countedItems((int) countedItems)
                .itemsRequiringAdjustment((int) itemsRequiringAdjustment)
                .approvedAdjustments((int) approvedAdjustments)
                .rejectedAdjustments((int) rejectedAdjustments)
                .build();

        // 항목 변환
        List<PhysicalInventoryItemResponse> itemResponses = entity.getItems().stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return PhysicalInventoryResponse.builder()
                .physicalInventoryId(entity.getPhysicalInventoryId())
                .inventoryNo(entity.getInventoryNo())
                .inventoryDate(entity.getInventoryDate())
                .warehouseId(entity.getWarehouse().getWarehouseId())
                .warehouseCode(entity.getWarehouse().getWarehouseCode())
                .warehouseName(entity.getWarehouse().getWarehouseName())
                .inventoryStatus(entity.getInventoryStatus())
                .plannedByUserId(entity.getPlannedByUserId())
                .approvedByUserId(entity.getApprovedByUserId())
                .approvalDate(entity.getApprovalDate())
                .remarks(entity.getRemarks())
                .items(itemResponses)
                .statistics(statistics)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Item Entity를 Response DTO로 변환
     */
    private PhysicalInventoryItemResponse toItemResponse(PhysicalInventoryItemEntity entity) {
        return PhysicalInventoryItemResponse.builder()
                .physicalInventoryItemId(entity.getPhysicalInventoryItemId())
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProduct().getProductCode())
                .productName(entity.getProduct().getProductName())
                .lotId(entity.getLot() != null ? entity.getLot().getLotId() : null)
                .lotNo(entity.getLot() != null ? entity.getLot().getLotNo() : null)
                .expiryDate(entity.getLot() != null ? entity.getLot().getExpiryDate() : null)
                .location(entity.getLocation())
                .systemQuantity(entity.getSystemQuantity())
                .countedQuantity(entity.getCountedQuantity())
                .differenceQuantity(entity.getDifferenceQuantity())
                .adjustmentStatus(entity.getAdjustmentStatus())
                .adjustmentTransactionId(entity.getAdjustmentTransaction() != null ?
                        entity.getAdjustmentTransaction().getTransactionId() : null)
                .countedByUserId(entity.getCountedByUserId())
                .countedAt(entity.getCountedAt())
                .remarks(entity.getRemarks())
                .unit(entity.getProduct().getUnit())
                .build();
    }
}
