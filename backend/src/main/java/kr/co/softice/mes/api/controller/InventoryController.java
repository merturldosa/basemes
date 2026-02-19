package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.inventory.InventoryReleaseRequest;
import kr.co.softice.mes.common.dto.inventory.InventoryReserveRequest;
import kr.co.softice.mes.common.dto.inventory.InventoryResponse;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.InventoryEntity;
import kr.co.softice.mes.domain.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Controller
 * 재고 현황 관리 API
 *
 * 엔드포인트:
 * - GET /api/inventory - 재고 현황 조회
 * - GET /api/inventory/{id} - 재고 상세 조회
 * - GET /api/inventory/warehouse/{warehouseId} - 창고별 재고
 * - GET /api/inventory/product/{productId} - 제품별 재고
 * - GET /api/inventory/low-stock - 저재고 알림
 * - POST /api/inventory/reserve - 재고 예약 (작업지시용)
 * - POST /api/inventory/release - 예약 해제
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory Management", description = "재고 현황 관리 API")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 재고 현황 조회
     * GET /api/inventory
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "재고 현황 조회", description = "테넌트의 모든 재고 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventory() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting inventory for tenant: {}", tenantId);

        List<InventoryEntity> inventories = inventoryService.findByTenant(tenantId);
        List<InventoryResponse> responses = inventories.stream()
                .map(this::toInventoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("재고 현황 조회 성공", responses));
    }

    /**
     * 재고 상세 조회
     * GET /api/inventory/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "재고 상세 조회", description = "재고 ID로 상세 정보 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(@PathVariable Long id) {
        log.info("Getting inventory: {}", id);

        InventoryEntity inventory = inventoryService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.INVENTORY_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("재고 조회 성공", toInventoryResponse(inventory)));
    }

    /**
     * 창고별 재고 조회
     * GET /api/inventory/warehouse/{warehouseId}
     */
    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "창고별 재고 조회", description = "특정 창고의 재고 목록 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryByWarehouse(
            @PathVariable Long warehouseId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting inventory for warehouse: {} in tenant: {}", warehouseId, tenantId);

        List<InventoryEntity> inventories = inventoryService.findByTenantAndWarehouse(tenantId, warehouseId);
        List<InventoryResponse> responses = inventories.stream()
                .map(this::toInventoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("창고별 재고 조회 성공", responses));
    }

    /**
     * 제품별 재고 조회
     * GET /api/inventory/product/{productId}
     */
    @GetMapping("/product/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "제품별 재고 조회", description = "특정 제품의 재고 목록 조회 (모든 창고)")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryByProduct(
            @PathVariable Long productId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting inventory for product: {} in tenant: {}", productId, tenantId);

        List<InventoryEntity> inventories = inventoryService.findByTenantAndProduct(tenantId, productId);
        List<InventoryResponse> responses = inventories.stream()
                .map(this::toInventoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("제품별 재고 조회 성공", responses));
    }

    /**
     * 저재고 알림
     * GET /api/inventory/low-stock
     *
     * Query Parameters:
     * - threshold: 기준 수량 (기본값: 100)
     */
    @GetMapping("/low-stock")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "저재고 알림", description = "기준 수량 미만인 재고 목록 조회")
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockInventory(
            @RequestParam(required = false, defaultValue = "100") BigDecimal threshold) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting low stock inventory for tenant: {}, threshold: {}", tenantId, threshold);

        List<InventoryEntity> lowStockItems = inventoryService.calculateLowStock(tenantId, threshold);
        List<InventoryResponse> responses = lowStockItems.stream()
                .map(this::toInventoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("저재고 조회 성공", responses));
    }

    /**
     * 재고 예약 (작업 지시용)
     * POST /api/inventory/reserve
     *
     * 가용 재고(available_quantity)를 예약 재고(reserved_quantity)로 이동
     */
    @PostMapping("/reserve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "재고 예약", description = "작업 지시를 위한 재고 예약 (가용 → 예약)")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveInventory(
            @Valid @RequestBody InventoryReserveRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Reserving inventory: warehouse={}, product={}, lot={}, quantity={}",
            request.getWarehouseId(), request.getProductId(), request.getLotId(), request.getQuantity());

        InventoryEntity reserved = inventoryService.reserveInventory(
            tenantId,
            request.getWarehouseId(),
            request.getProductId(),
            request.getLotId(),
            request.getQuantity()
        );

        return ResponseEntity.ok(ApiResponse.success("재고 예약 성공", toInventoryResponse(reserved)));
    }

    /**
     * 재고 예약 해제
     * POST /api/inventory/release
     *
     * 예약 재고(reserved_quantity)를 가용 재고(available_quantity)로 되돌림
     */
    @PostMapping("/release")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "재고 예약 해제", description = "예약된 재고를 가용 재고로 되돌림 (예약 → 가용)")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseInventory(
            @Valid @RequestBody InventoryReleaseRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Releasing inventory: warehouse={}, product={}, lot={}, quantity={}",
            request.getWarehouseId(), request.getProductId(), request.getLotId(), request.getQuantity());

        InventoryEntity released = inventoryService.releaseReservedInventory(
            tenantId,
            request.getWarehouseId(),
            request.getProductId(),
            request.getLotId(),
            request.getQuantity()
        );

        return ResponseEntity.ok(ApiResponse.success("재고 예약 해제 성공", toInventoryResponse(released)));
    }

    // ================== Private Helper Methods ==================

    /**
     * Convert entity to response DTO
     */
    private InventoryResponse toInventoryResponse(InventoryEntity entity) {
        String location = buildLocation(entity.getZone(), entity.getRack(), entity.getShelf(), entity.getBin());

        return InventoryResponse.builder()
                .inventoryId(entity.getInventoryId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .warehouseId(entity.getWarehouse().getWarehouseId())
                .warehouseCode(entity.getWarehouse().getWarehouseCode())
                .warehouseName(entity.getWarehouse().getWarehouseName())
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProduct().getProductCode())
                .productName(entity.getProduct().getProductName())
                .lotId(entity.getLot() != null ? entity.getLot().getLotId() : null)
                .lotNo(entity.getLot() != null ? entity.getLot().getLotNo() : null)
                .availableQuantity(entity.getAvailableQuantity())
                .reservedQuantity(entity.getReservedQuantity())
                .unit(entity.getUnit())
                .location(location)
                .lastTransactionDate(entity.getLastTransactionDate())
                .lastTransactionType(entity.getLastTransactionType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Build location string from zone, rack, shelf, bin
     */
    private String buildLocation(String zone, String rack, String shelf, String bin) {
        StringBuilder sb = new StringBuilder();
        if (zone != null && !zone.isEmpty()) {
            sb.append(zone);
        }
        if (rack != null && !rack.isEmpty()) {
            if (sb.length() > 0) sb.append("-");
            sb.append(rack);
        }
        if (shelf != null && !shelf.isEmpty()) {
            if (sb.length() > 0) sb.append("-");
            sb.append(shelf);
        }
        if (bin != null && !bin.isEmpty()) {
            if (sb.length() > 0) sb.append("-");
            sb.append(bin);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
