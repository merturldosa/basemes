package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.inventory.InventoryTransactionCreateRequest;
import kr.co.softice.mes.common.dto.inventory.InventoryTransactionResponse;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.InventoryTransactionService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inventory Transaction Controller
 * 재고 이동 내역 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/inventory-transactions")
@RequiredArgsConstructor
@Tag(name = "Inventory Transaction", description = "재고 이동 내역 관리 API")
public class InventoryTransactionController {

    private final InventoryTransactionService inventoryTransactionService;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final kr.co.softice.mes.domain.repository.TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<InventoryTransactionResponse>>> getAllTransactions() {
        String tenantId = TenantContext.getCurrentTenant();
        List<InventoryTransactionEntity> transactions = inventoryTransactionService.findByTenant(tenantId);
        List<InventoryTransactionResponse> responses = transactions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("재고 이동 목록 조회 성공", responses));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{transactionId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_MANAGER', 'USER')")
    public ResponseEntity<InventoryTransactionResponse> getTransactionById(@PathVariable Long transactionId) {
        return inventoryTransactionService.findById(transactionId)
            .map(transaction -> ResponseEntity.ok(toResponse(transaction)))
            .orElse(ResponseEntity.notFound().build());
    }

    @Transactional(readOnly = true)
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_MANAGER', 'USER')")
    public ResponseEntity<List<InventoryTransactionResponse>> getTransactionsByDateRange(
        @RequestParam LocalDateTime startDate,
        @RequestParam LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        List<InventoryTransactionEntity> transactions =
            inventoryTransactionService.findByDateRange(tenantId, startDate, endDate);
        return ResponseEntity.ok(transactions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/approval-status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_MANAGER', 'USER')")
    public ResponseEntity<List<InventoryTransactionResponse>> getTransactionsByApprovalStatus(
        @PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting transactions by approval status: {} for tenant: {}", status, tenantId);

        List<InventoryTransactionEntity> transactions =
            inventoryTransactionService.findByApprovalStatus(tenantId, status);
        return ResponseEntity.ok(transactions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryTransactionResponse> createTransaction(
        @Valid @RequestBody InventoryTransactionCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        WarehouseEntity warehouse = warehouseRepository.findById(request.getWarehouseId())
            .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));

        ProductEntity product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        LotEntity lot = null;
        if (request.getLotId() != null) {
            lot = lotRepository.findById(request.getLotId())
                .orElseThrow(() -> new IllegalArgumentException("Lot not found"));
        }

        UserEntity transactionUser = userRepository.findById(request.getTransactionUserId())
            .orElseThrow(() -> new IllegalArgumentException("Transaction user not found"));

        // For MOVE transactions
        WarehouseEntity fromWarehouse = null;
        WarehouseEntity toWarehouse = null;
        if ("MOVE".equals(request.getTransactionType())) {
            if (request.getFromWarehouseId() != null) {
                fromWarehouse = warehouseRepository.findById(request.getFromWarehouseId())
                    .orElseThrow(() -> new IllegalArgumentException("From warehouse not found"));
            }
            if (request.getToWarehouseId() != null) {
                toWarehouse = warehouseRepository.findById(request.getToWarehouseId())
                    .orElseThrow(() -> new IllegalArgumentException("To warehouse not found"));
            }
        }

        // Optional references
        WorkOrderEntity workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Work order not found"));
        }

        QualityInspectionEntity qualityInspection = null;
        if (request.getQualityInspectionId() != null) {
            qualityInspection = qualityInspectionRepository.findById(request.getQualityInspectionId())
                .orElseThrow(() -> new IllegalArgumentException("Quality inspection not found"));
        }

        InventoryTransactionEntity transaction = InventoryTransactionEntity.builder()
            .tenant(tenant)
            .transactionNo(request.getTransactionNo())
            .transactionType(request.getTransactionType())
            .transactionDate(request.getTransactionDate())
            .warehouse(warehouse)
            .product(product)
            .lot(lot)
            .quantity(request.getQuantity())
            .unit(request.getUnit())
            .fromWarehouse(fromWarehouse)
            .toWarehouse(toWarehouse)
            .workOrder(workOrder)
            .qualityInspection(qualityInspection)
            .transactionUser(transactionUser)
            .referenceNo(request.getReferenceNo())
            .remarks(request.getRemarks())
            .build();

        InventoryTransactionEntity created = inventoryTransactionService.createTransaction(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    /**
     * Approve transaction
     * POST /api/inventory-transactions/{id}/approve
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryTransactionResponse> approveTransaction(
        @PathVariable Long id,
        @RequestParam Long approverId) {

        log.info("Approving transaction: {} by user: {}", id, approverId);

        InventoryTransactionEntity approved = inventoryTransactionService.approveTransaction(id, approverId);

        return ResponseEntity.ok(toResponse(approved));
    }

    /**
     * Reject transaction
     * POST /api/inventory-transactions/{id}/reject
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryTransactionResponse> rejectTransaction(
        @PathVariable Long id,
        @RequestParam Long approverId,
        @RequestParam String reason) {

        log.info("Rejecting transaction: {} by user: {}, reason: {}", id, approverId, reason);

        InventoryTransactionEntity rejected = inventoryTransactionService.rejectTransaction(id, approverId, reason);

        return ResponseEntity.ok(toResponse(rejected));
    }

    private InventoryTransactionResponse toResponse(InventoryTransactionEntity transaction) {
        return InventoryTransactionResponse.builder()
            .transactionId(transaction.getTransactionId())
            .tenantId(transaction.getTenant().getTenantId())
            .tenantName(transaction.getTenant().getTenantName())
            .transactionNo(transaction.getTransactionNo())
            .transactionType(transaction.getTransactionType())
            .transactionDate(transaction.getTransactionDate())
            .warehouseId(transaction.getWarehouse().getWarehouseId())
            .warehouseCode(transaction.getWarehouse().getWarehouseCode())
            .warehouseName(transaction.getWarehouse().getWarehouseName())
            .productId(transaction.getProduct().getProductId())
            .productCode(transaction.getProduct().getProductCode())
            .productName(transaction.getProduct().getProductName())
            .lotId(transaction.getLot() != null ? transaction.getLot().getLotId() : null)
            .lotNo(transaction.getLot() != null ? transaction.getLot().getLotNo() : null)
            .quantity(transaction.getQuantity())
            .unit(transaction.getUnit())
            .fromWarehouseId(transaction.getFromWarehouse() != null ? transaction.getFromWarehouse().getWarehouseId() : null)
            .fromWarehouseCode(transaction.getFromWarehouse() != null ? transaction.getFromWarehouse().getWarehouseCode() : null)
            .fromWarehouseName(transaction.getFromWarehouse() != null ? transaction.getFromWarehouse().getWarehouseName() : null)
            .toWarehouseId(transaction.getToWarehouse() != null ? transaction.getToWarehouse().getWarehouseId() : null)
            .toWarehouseCode(transaction.getToWarehouse() != null ? transaction.getToWarehouse().getWarehouseCode() : null)
            .toWarehouseName(transaction.getToWarehouse() != null ? transaction.getToWarehouse().getWarehouseName() : null)
            .workOrderId(transaction.getWorkOrder() != null ? transaction.getWorkOrder().getWorkOrderId() : null)
            .workOrderNo(transaction.getWorkOrder() != null ? transaction.getWorkOrder().getWorkOrderNo() : null)
            .qualityInspectionId(transaction.getQualityInspection() != null ? transaction.getQualityInspection().getQualityInspectionId() : null)
            .inspectionNo(transaction.getQualityInspection() != null ? transaction.getQualityInspection().getInspectionNo() : null)
            .transactionUserId(transaction.getTransactionUser().getUserId())
            .transactionUserName(transaction.getTransactionUser().getUsername())
            .approvalStatus(transaction.getApprovalStatus())
            .approvedById(transaction.getApprovedBy() != null ? transaction.getApprovedBy().getUserId() : null)
            .approvedByName(transaction.getApprovedBy() != null ? transaction.getApprovedBy().getUsername() : null)
            .approvedDate(transaction.getApprovedDate())
            .referenceNo(transaction.getReferenceNo())
            .remarks(transaction.getRemarks())
            .createdAt(transaction.getCreatedAt())
            .updatedAt(transaction.getUpdatedAt())
            .build();
    }
}
