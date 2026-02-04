package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.wms.*;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import kr.co.softice.mes.domain.service.DisposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/disposals")
@RequiredArgsConstructor
@Tag(name = "Disposal Management", description = "폐기 관리 API")
public class DisposalController {

    private final DisposalService disposalService;
    private final TenantRepository tenantRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;
    private final LotRepository lotRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "폐기 목록 조회")
    public ResponseEntity<ApiResponse<List<DisposalResponse>>> getDisposals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long warehouseId) {

        String tenantId = TenantContext.getCurrentTenant();
        List<DisposalEntity> disposals;

        if (status != null) {
            disposals = disposalService.findByStatus(tenantId, status);
        } else if (type != null) {
            disposals = disposalService.findByType(tenantId, type);
        } else if (warehouseId != null) {
            disposals = disposalService.findByWarehouseId(tenantId, warehouseId);
        } else {
            disposals = disposalService.findByTenant(tenantId);
        }

        List<DisposalResponse> responses = disposals.stream()
                .map(this::toDisposalResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("폐기 목록 조회 성공", responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "폐기 상세 조회")
    public ResponseEntity<ApiResponse<DisposalResponse>> getDisposal(@PathVariable Long id) {
        DisposalEntity disposal = disposalService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.DISPOSAL_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("폐기 조회 성공", toDisposalResponse(disposal)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "폐기 생성")
    public ResponseEntity<ApiResponse<DisposalResponse>> createDisposal(
            @Valid @RequestBody DisposalCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        UserEntity requester = userRepository.findById(request.getRequesterUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));

        WarehouseEntity warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WAREHOUSE_NOT_FOUND));

        WorkOrderEntity workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WORK_ORDER_NOT_FOUND));
        }

        DisposalEntity disposal = DisposalEntity.builder()
                .tenant(tenant)
                .disposalNo(request.getDisposalNo())
                .disposalDate(request.getDisposalDate())
                .disposalType(request.getDisposalType())
                .workOrder(workOrder)
                .requester(requester)
                .warehouse(warehouse)
                .remarks(request.getRemarks())
                .isActive(true)
                .build();

        for (DisposalItemRequest itemReq : request.getItems()) {
            ProductEntity product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

            LotEntity lot = null;
            if (itemReq.getLotId() != null) {
                lot = lotRepository.findById(itemReq.getLotId())
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.LOT_NOT_FOUND));
            }

            DisposalItemEntity item = DisposalItemEntity.builder()
                    .disposal(disposal)
                    .product(product)
                    .lot(lot)
                    .disposalQuantity(itemReq.getDisposalQuantity())
                    .defectType(itemReq.getDefectType())
                    .defectDescription(itemReq.getDefectDescription())
                    .expiryDate(itemReq.getExpiryDate())
                    .remarks(itemReq.getRemarks())
                    .build();

            disposal.addItem(item);
        }

        DisposalEntity created = disposalService.createDisposal(disposal);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("폐기 생성 성공", toDisposalResponse(created)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 승인")
    public ResponseEntity<ApiResponse<DisposalResponse>> approveDisposal(
            @PathVariable Long id,
            @RequestParam Long approverUserId) {

        DisposalEntity approved = disposalService.approveDisposal(id, approverUserId);
        return ResponseEntity.ok(ApiResponse.success("폐기 승인 성공", toDisposalResponse(approved)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 거부")
    public ResponseEntity<ApiResponse<DisposalResponse>> rejectDisposal(
            @PathVariable Long id,
            @RequestParam Long approverUserId,
            @RequestParam String reason) {

        DisposalEntity rejected = disposalService.rejectDisposal(id, approverUserId, reason);
        return ResponseEntity.ok(ApiResponse.success("폐기 거부 성공", toDisposalResponse(rejected)));
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'INVENTORY_CLERK')")
    @Operation(summary = "폐기 처리", description = "재고 차감 및 LOT 비활성화")
    public ResponseEntity<ApiResponse<DisposalResponse>> processDisposal(
            @PathVariable Long id,
            @RequestParam Long processorUserId) {

        DisposalEntity processed = disposalService.processDisposal(id, processorUserId);
        return ResponseEntity.ok(ApiResponse.success("폐기 처리 성공", toDisposalResponse(processed)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 완료")
    public ResponseEntity<ApiResponse<DisposalResponse>> completeDisposal(
            @PathVariable Long id,
            @RequestParam String method,
            @RequestParam String location) {

        DisposalEntity completed = disposalService.completeDisposal(id, method, location);
        return ResponseEntity.ok(ApiResponse.success("폐기 완료 성공", toDisposalResponse(completed)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER')")
    @Operation(summary = "폐기 취소")
    public ResponseEntity<ApiResponse<DisposalResponse>> cancelDisposal(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {

        DisposalEntity cancelled = disposalService.cancelDisposal(id,
            reason != null ? reason : "Cancelled by user");
        return ResponseEntity.ok(ApiResponse.success("폐기 취소 성공", toDisposalResponse(cancelled)));
    }

    private DisposalResponse toDisposalResponse(DisposalEntity entity) {
        return DisposalResponse.builder()
                .disposalId(entity.getDisposalId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .disposalNo(entity.getDisposalNo())
                .disposalDate(entity.getDisposalDate())
                .disposalType(entity.getDisposalType())
                .disposalStatus(entity.getDisposalStatus())
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderNo() : null)
                .requesterUserId(entity.getRequester().getUserId())
                .requesterUserName(entity.getRequester().getUsername())
                .requesterName(entity.getRequesterName())
                .warehouseId(entity.getWarehouse().getWarehouseId())
                .warehouseCode(entity.getWarehouse().getWarehouseCode())
                .warehouseName(entity.getWarehouse().getWarehouseName())
                .approverUserId(entity.getApprover() != null ? entity.getApprover().getUserId() : null)
                .approverUserName(entity.getApprover() != null ? entity.getApprover().getUsername() : null)
                .approverName(entity.getApproverName())
                .approvedDate(entity.getApprovedDate())
                .processorUserId(entity.getProcessor() != null ? entity.getProcessor().getUserId() : null)
                .processorUserName(entity.getProcessor() != null ? entity.getProcessor().getUsername() : null)
                .processorName(entity.getProcessorName())
                .processedDate(entity.getProcessedDate())
                .completedDate(entity.getCompletedDate())
                .disposalMethod(entity.getDisposalMethod())
                .disposalLocation(entity.getDisposalLocation())
                .totalDisposalQuantity(entity.getTotalDisposalQuantity())
                .items(entity.getItems().stream().map(this::toDisposalItemResponse).collect(Collectors.toList()))
                .remarks(entity.getRemarks())
                .rejectionReason(entity.getRejectionReason())
                .cancellationReason(entity.getCancellationReason())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedAt(entity.getUpdatedAt())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }

    private DisposalItemResponse toDisposalItemResponse(DisposalItemEntity entity) {
        return DisposalItemResponse.builder()
                .disposalItemId(entity.getDisposalItemId())
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .productType(entity.getProduct().getProductType())
                .unit(entity.getProduct().getUnit())
                .lotId(entity.getLot() != null ? entity.getLot().getLotId() : null)
                .lotNo(entity.getLotNo())
                .disposalQuantity(entity.getDisposalQuantity())
                .processedQuantity(entity.getProcessedQuantity())
                .disposalTransactionId(entity.getDisposalTransaction() != null ?
                    entity.getDisposalTransaction().getInventoryTransactionId() : null)
                .defectType(entity.getDefectType())
                .defectDescription(entity.getDefectDescription())
                .expiryDate(entity.getExpiryDate())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
