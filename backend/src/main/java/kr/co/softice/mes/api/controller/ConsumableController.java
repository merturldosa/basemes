package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.ConsumableCreateRequest;
import kr.co.softice.mes.common.dto.equipment.ConsumableResponse;
import kr.co.softice.mes.common.dto.equipment.ConsumableUpdateRequest;
import kr.co.softice.mes.domain.entity.ConsumableEntity;
import kr.co.softice.mes.domain.service.ConsumableService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Consumable Controller
 * 소모품 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/consumables")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consumable", description = "소모품 관리 API")
public class ConsumableController {

    private final ConsumableService consumableService;

    /**
     * Get all consumables
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "소모품 목록 조회", description = "모든 소모품을 조회합니다.")
    public ResponseEntity<List<ConsumableResponse>> getAllConsumables() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all consumables for tenant: {}", tenantId);

        List<ConsumableEntity> consumables = consumableService.getAllConsumables(tenantId);
        List<ConsumableResponse> response = consumables.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get consumable by ID
     */
    @GetMapping("/{consumableId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "소모품 상세 조회", description = "ID로 소모품을 조회합니다.")
    public ResponseEntity<ConsumableResponse> getConsumableById(@PathVariable Long consumableId) {
        log.info("Getting consumable by ID: {}", consumableId);

        ConsumableEntity consumable = consumableService.getConsumableById(consumableId);
        ConsumableResponse response = toResponse(consumable);

        return ResponseEntity.ok(response);
    }

    /**
     * Get low stock consumables
     */
    @GetMapping("/low-stock")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "재고 부족 소모품 조회", description = "현재 재고가 최소 재고 이하인 소모품을 조회합니다.")
    public ResponseEntity<List<ConsumableResponse>> getLowStock() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting low stock consumables for tenant: {}", tenantId);

        List<ConsumableEntity> consumables = consumableService.getLowStock(tenantId);
        List<ConsumableResponse> response = consumables.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Create consumable
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "소모품 등록", description = "새로운 소모품을 등록합니다.")
    public ResponseEntity<ConsumableResponse> createConsumable(@Valid @RequestBody ConsumableCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating consumable: {} for tenant: {}", request.getConsumableCode(), tenantId);

        ConsumableEntity consumable = toEntity(request);
        ConsumableEntity created = consumableService.createConsumable(tenantId, consumable, request.getEquipmentId());
        ConsumableResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update consumable
     */
    @PutMapping("/{consumableId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "소모품 수정", description = "소모품 정보를 수정합니다.")
    public ResponseEntity<ConsumableResponse> updateConsumable(
            @PathVariable Long consumableId,
            @Valid @RequestBody ConsumableUpdateRequest request) {
        log.info("Updating consumable ID: {}", consumableId);

        ConsumableEntity updateData = toEntity(request);
        ConsumableEntity updated = consumableService.updateConsumable(consumableId, updateData, request.getEquipmentId());
        ConsumableResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Adjust stock
     */
    @PostMapping("/{consumableId}/adjust-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "소모품 재고 조정", description = "소모품 재고를 조정합니다. 양수는 입고, 음수는 출고입니다.")
    public ResponseEntity<ConsumableResponse> adjustStock(
            @PathVariable Long consumableId,
            @RequestParam BigDecimal quantity) {
        log.info("Adjusting stock for consumable ID: {} by {}", consumableId, quantity);

        ConsumableEntity adjusted = consumableService.adjustStock(consumableId, quantity);
        ConsumableResponse response = toResponse(adjusted);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete consumable
     */
    @DeleteMapping("/{consumableId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "소모품 삭제", description = "소모품을 삭제합니다.")
    public ResponseEntity<Void> deleteConsumable(@PathVariable Long consumableId) {
        log.info("Deleting consumable ID: {}", consumableId);

        consumableService.deleteConsumable(consumableId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert ConsumableCreateRequest to Entity
     */
    private ConsumableEntity toEntity(ConsumableCreateRequest request) {
        return ConsumableEntity.builder()
                .consumableCode(request.getConsumableCode())
                .consumableName(request.getConsumableName())
                .category(request.getCategory())
                .unit(request.getUnit())
                .currentStock(request.getCurrentStock())
                .minimumStock(request.getMinimumStock())
                .maximumStock(request.getMaximumStock())
                .unitPrice(request.getUnitPrice())
                .supplier(request.getSupplier())
                .leadTimeDays(request.getLeadTimeDays())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert ConsumableUpdateRequest to Entity
     */
    private ConsumableEntity toEntity(ConsumableUpdateRequest request) {
        return ConsumableEntity.builder()
                .consumableName(request.getConsumableName())
                .category(request.getCategory())
                .unit(request.getUnit())
                .currentStock(request.getCurrentStock())
                .minimumStock(request.getMinimumStock())
                .maximumStock(request.getMaximumStock())
                .unitPrice(request.getUnitPrice())
                .supplier(request.getSupplier())
                .leadTimeDays(request.getLeadTimeDays())
                .status(request.getStatus())
                .remarks(request.getRemarks())
                .build();
    }

    /**
     * Convert Entity to ConsumableResponse
     */
    private ConsumableResponse toResponse(ConsumableEntity entity) {
        return ConsumableResponse.builder()
                .consumableId(entity.getConsumableId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .consumableCode(entity.getConsumableCode())
                .consumableName(entity.getConsumableName())
                .category(entity.getCategory())
                .equipmentId(entity.getEquipment() != null ? entity.getEquipment().getEquipmentId() : null)
                .equipmentCode(entity.getEquipment() != null ? entity.getEquipment().getEquipmentCode() : null)
                .equipmentName(entity.getEquipment() != null ? entity.getEquipment().getEquipmentName() : null)
                .unit(entity.getUnit())
                .currentStock(entity.getCurrentStock())
                .minimumStock(entity.getMinimumStock())
                .maximumStock(entity.getMaximumStock())
                .unitPrice(entity.getUnitPrice())
                .supplier(entity.getSupplier())
                .leadTimeDays(entity.getLeadTimeDays())
                .lastReplenishedDate(entity.getLastReplenishedDate())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
