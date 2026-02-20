package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.wms.LotAllocationResponse;
import kr.co.softice.mes.common.dto.wms.LotSelectionRequest;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.service.LotSelectionService;
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
 * LOT 선택 컨트롤러
 * FIFO/FEFO 기반 LOT 자동 선택 API 제공
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/lot-selection")
@RequiredArgsConstructor
@Tag(name = "LOT Selection", description = "LOT 선택 API")
public class LotSelectionController {

    private final LotSelectionService lotSelectionService;

    /**
     * FIFO 전략으로 LOT 선택
     */
    @PostMapping("/fifo")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "FIFO LOT 선택", description = "가장 오래된 LOT부터 선택")
    public ResponseEntity<ApiResponse<List<LotAllocationResponse>>> selectByFIFO(
            @Valid @RequestBody LotSelectionRequest request) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("FIFO LOT selection request - Tenant: {}, Warehouse: {}, Product: {}, Quantity: {}",
                tenantId, request.getWarehouseId(), request.getProductId(), request.getRequiredQuantity());

        List<LotSelectionService.LotAllocation> allocations = lotSelectionService.selectLotsByFIFO(
                tenantId,
                request.getWarehouseId(),
                request.getProductId(),
                request.getRequiredQuantity()
        );

        List<LotAllocationResponse> responses = allocations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("FIFO LOT 선택 완료", responses)
        );
    }

    /**
     * FEFO 전략으로 LOT 선택
     */
    @PostMapping("/fefo")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "FEFO LOT 선택", description = "유효기간이 빠른 LOT부터 선택")
    public ResponseEntity<ApiResponse<List<LotAllocationResponse>>> selectByFEFO(
            @Valid @RequestBody LotSelectionRequest request) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("FEFO LOT selection request - Tenant: {}, Warehouse: {}, Product: {}, Quantity: {}",
                tenantId, request.getWarehouseId(), request.getProductId(), request.getRequiredQuantity());

        List<LotSelectionService.LotAllocation> allocations = lotSelectionService.selectLotsByFEFO(
                tenantId,
                request.getWarehouseId(),
                request.getProductId(),
                request.getRequiredQuantity()
        );

        List<LotAllocationResponse> responses = allocations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success("FEFO LOT 선택 완료", responses)
        );
    }

    /**
     * 특정 LOT 선택
     */
    @PostMapping("/specific")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "특정 LOT 선택", description = "사용자 지정 LOT 선택")
    public ResponseEntity<ApiResponse<LotAllocationResponse>> selectSpecific(
            @Valid @RequestBody LotSelectionRequest request) {

        String tenantId = TenantContext.getCurrentTenant();

        if (request.getLotId() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("LOT ID가 필요합니다.")
            );
        }

        log.info("Specific LOT selection request - Tenant: {}, LOT: {}, Quantity: {}",
                tenantId, request.getLotId(), request.getRequiredQuantity());

        LotSelectionService.LotAllocation allocation = lotSelectionService.selectSpecificLot(
                tenantId,
                request.getWarehouseId(),
                request.getProductId(),
                request.getLotId(),
                request.getRequiredQuantity()
        );

        return ResponseEntity.ok(
                ApiResponse.success("LOT 선택 완료", toResponse(allocation))
        );
    }

    /**
     * 만료 예정 LOT 조회
     */
    @Transactional(readOnly = true)
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'QMS_MANAGER')")
    @Operation(summary = "만료 예정 LOT 조회", description = "지정된 일수 내에 만료되는 LOT 목록 반환")
    public ResponseEntity<ApiResponse<List<LotExpiryInfo>>> getExpiringLots(
            @RequestParam(defaultValue = "30") int daysUntilExpiry) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("Expiring lots request - Tenant: {}, Days: {}", tenantId, daysUntilExpiry);

        List<LotEntity> expiringLots = lotSelectionService.findExpiringLots(tenantId, daysUntilExpiry);

        List<LotExpiryInfo> responses = expiringLots.stream()
                .map(lot -> LotExpiryInfo.builder()
                        .lotId(lot.getLotId())
                        .lotNo(lot.getLotNo())
                        .productCode(lot.getProduct().getProductCode())
                        .productName(lot.getProduct().getProductName())
                        .expiryDate(lot.getExpiryDate())
                        .currentQuantity(lot.getCurrentQuantity())
                        .qualityStatus(lot.getQualityStatus())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("%d일 내 만료 예정 LOT %d건 조회 완료", daysUntilExpiry, responses.size()),
                        responses
                )
        );
    }

    /**
     * LotAllocation을 Response DTO로 변환
     */
    private LotAllocationResponse toResponse(LotSelectionService.LotAllocation allocation) {
        return LotAllocationResponse.builder()
                .lotId(allocation.getLotId())
                .lotNo(allocation.getLotNo())
                .allocatedQuantity(allocation.getAllocatedQuantity())
                .availableQuantity(allocation.getAvailableQuantity())
                .expiryDate(allocation.getExpiryDate())
                .build();
    }

    /**
     * LOT 만료 정보 DTO
     */
    @lombok.Getter
    @lombok.Builder
    private static class LotExpiryInfo {
        private Long lotId;
        private String lotNo;
        private String productCode;
        private String productName;
        private java.time.LocalDate expiryDate;
        private java.math.BigDecimal currentQuantity;
        private String qualityStatus;
    }
}
