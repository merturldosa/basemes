package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.inventory.LotCreateRequest;
import kr.co.softice.mes.common.dto.inventory.LotResponse;
import kr.co.softice.mes.common.dto.inventory.LotSplitRequest;
import kr.co.softice.mes.common.dto.inventory.LotUpdateRequest;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.WorkOrderEntity;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.WorkOrderRepository;
import kr.co.softice.mes.domain.service.LotService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lot Controller
 * LOT/배치 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/lots")
@RequiredArgsConstructor
@Tag(name = "Lot", description = "LOT/배치 관리 API")
public class LotController {

    private final LotService lotService;
    private final ProductRepository productRepository;
    private final TenantRepository tenantRepository;
    private final WorkOrderRepository workOrderRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'QUALITY_MANAGER', 'USER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<LotResponse>>> getAllLots() {
        String tenantId = TenantContext.getCurrentTenant();
        List<LotEntity> lots = lotService.findByTenant(tenantId);
        List<LotResponse> responses = lots.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("LOT 목록 조회 성공", responses));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'QUALITY_MANAGER', 'USER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<LotResponse>>> getLotsByProduct(@PathVariable Long productId) {
        String tenantId = TenantContext.getCurrentTenant();
        List<LotEntity> lots = lotService.findByTenantAndProduct(tenantId, productId);
        List<LotResponse> responses = lots.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("제품별 LOT 조회 성공", responses));
    }

    @GetMapping("/quality-status/{qualityStatus}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'QUALITY_MANAGER', 'USER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<LotResponse>>> getLotsByQualityStatus(@PathVariable String qualityStatus) {
        String tenantId = TenantContext.getCurrentTenant();
        List<LotEntity> lots = lotService.findByTenantAndQualityStatus(tenantId, qualityStatus);
        List<LotResponse> responses = lots.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("품질상태별 LOT 조회 성공", responses));
    }

    @GetMapping("/{lotId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER', 'QUALITY_MANAGER', 'USER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<LotResponse>> getLotById(@PathVariable Long lotId) {
        return lotService.findById(lotId)
            .map(lot -> ResponseEntity.ok(ApiResponse.success("LOT 조회 성공", toResponse(lot))))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<LotResponse> createLot(@Valid @RequestBody LotCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        ProductEntity product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        WorkOrderEntity workOrder = null;
        if (request.getWorkOrderId() != null) {
            workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Work order not found"));
        }

        LotEntity lot = LotEntity.builder()
            .tenant(tenant)
            .lotNo(request.getLotNo())
            .product(product)
            .workOrder(workOrder)
            .initialQuantity(request.getInitialQuantity())
            .currentQuantity(request.getCurrentQuantity())
            .unit(request.getUnit())
            .manufacturingDate(request.getManufactureDate())
            .expiryDate(request.getExpiryDate())
            .qualityStatus(request.getQualityStatus())
            .supplierName(request.getSupplier())
            .supplierLotNo(request.getSupplierLotNo())
            .remarks(request.getRemarks())
            .build();

        LotEntity created = lotService.createLot(lot);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{lotId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<LotResponse> updateLot(
        @PathVariable Long lotId,
        @Valid @RequestBody LotUpdateRequest request) {

        return lotService.findById(lotId)
            .map(lot -> {
                lot.setCurrentQuantity(request.getCurrentQuantity());
                lot.setManufacturingDate(request.getManufactureDate());
                lot.setExpiryDate(request.getExpiryDate());
                lot.setQualityStatus(request.getQualityStatus());
                lot.setSupplierName(request.getSupplier());
                lot.setSupplierLotNo(request.getSupplierLotNo());
                lot.setRemarks(request.getRemarks());

                LotEntity updated = lotService.updateLot(lot);
                return ResponseEntity.ok(toResponse(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{lotId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLot(@PathVariable Long lotId) {
        lotService.deleteLot(lotId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{lotId}/split")
    @PreAuthorize("hasAnyRole('ADMIN', 'INVENTORY_MANAGER')")
    public ResponseEntity<LotResponse> splitLot(
        @PathVariable Long lotId,
        @Valid @RequestBody LotSplitRequest request) {
        log.info("Splitting lot ID: {} with quantity: {}", lotId, request.getSplitQuantity());
        LotEntity childLot = lotService.splitLot(lotId, request.getSplitQuantity(), request.getRemarks());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(childLot));
    }

    @PostMapping("/{lotId}/quality-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    public ResponseEntity<LotResponse> updateQualityStatus(
        @PathVariable Long lotId,
        @RequestParam String qualityStatus) {
        LotEntity lot = lotService.updateQualityStatus(lotId, qualityStatus);
        return ResponseEntity.ok(toResponse(lot));
    }

    private LotResponse toResponse(LotEntity lot) {
        return LotResponse.builder()
            .lotId(lot.getLotId())
            .tenantId(lot.getTenant().getTenantId())
            .tenantName(lot.getTenant().getTenantName())
            .lotNo(lot.getLotNo())
            .productId(lot.getProduct().getProductId())
            .productCode(lot.getProduct().getProductCode())
            .productName(lot.getProduct().getProductName())
            .workOrderId(lot.getWorkOrder() != null ? lot.getWorkOrder().getWorkOrderId() : null)
            .workOrderNo(lot.getWorkOrder() != null ? lot.getWorkOrder().getWorkOrderNo() : null)
            .initialQuantity(lot.getInitialQuantity())
            .currentQuantity(lot.getCurrentQuantity())
            .unit(lot.getUnit())
            .manufactureDate(lot.getManufacturingDate())
            .expiryDate(lot.getExpiryDate())
            .qualityStatus(lot.getQualityStatus())
            .supplier(lot.getSupplierName())
            .supplierLotNo(lot.getSupplierLotNo())
            .remarks(lot.getRemarks())
            .createdAt(lot.getCreatedAt())
            .updatedAt(lot.getUpdatedAt())
            .build();
    }
}
