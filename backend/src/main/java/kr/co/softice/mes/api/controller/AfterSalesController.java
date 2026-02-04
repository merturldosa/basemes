package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.defect.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.AfterSalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * After Sales Controller
 * A/S 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/after-sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "After Sales", description = "A/S 관리 API")
public class AfterSalesController {

    private final AfterSalesService afterSalesService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER', 'SERVICE_ENGINEER')")
    @Operation(summary = "Get all after sales", description = "모든 A/S 조회")
    public ResponseEntity<List<AfterSalesResponse>> getAllAfterSales() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/after-sales - tenant: {}", tenantId);

        List<AfterSalesEntity> afterSales = afterSalesService.getAllAfterSales(tenantId);
        List<AfterSalesResponse> responses = afterSales.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER', 'SERVICE_ENGINEER')")
    @Operation(summary = "Get after sales by ID", description = "ID로 A/S 조회")
    public ResponseEntity<AfterSalesResponse> getAfterSalesById(@PathVariable Long id) {
        log.info("GET /api/after-sales/{}", id);

        AfterSalesEntity afterSales = afterSalesService.getAfterSalesById(id);
        return ResponseEntity.ok(toResponse(afterSales));
    }

    @GetMapping("/status/{serviceStatus}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER', 'SERVICE_ENGINEER')")
    @Operation(summary = "Get after sales by service status", description = "서비스 상태별 A/S 조회")
    public ResponseEntity<List<AfterSalesResponse>> getAfterSalesByServiceStatus(@PathVariable String serviceStatus) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/after-sales/status/{} - tenant: {}", serviceStatus, tenantId);

        List<AfterSalesEntity> afterSales = afterSalesService.getAfterSalesByServiceStatus(tenantId, serviceStatus);
        List<AfterSalesResponse> responses = afterSales.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/priority/{priority}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER', 'SERVICE_ENGINEER')")
    @Operation(summary = "Get after sales by priority", description = "우선순위별 A/S 조회")
    public ResponseEntity<List<AfterSalesResponse>> getAfterSalesByPriority(@PathVariable String priority) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/after-sales/priority/{} - tenant: {}", priority, tenantId);

        List<AfterSalesEntity> afterSales = afterSalesService.getAfterSalesByPriority(tenantId, priority);
        List<AfterSalesResponse> responses = afterSales.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER')")
    @Operation(summary = "Create after sales", description = "A/S 생성")
    public ResponseEntity<AfterSalesResponse> createAfterSales(@Valid @RequestBody AfterSalesCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/after-sales - tenant: {}, asNo: {}", tenantId, request.getAsNo());

        AfterSalesEntity entity = toEntity(request);
        AfterSalesEntity created = afterSalesService.createAfterSales(tenantId, entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER', 'SERVICE_ENGINEER')")
    @Operation(summary = "Update after sales", description = "A/S 수정")
    public ResponseEntity<AfterSalesResponse> updateAfterSales(
            @PathVariable Long id,
            @Valid @RequestBody AfterSalesUpdateRequest request) {
        log.info("PUT /api/after-sales/{}", id);

        AfterSalesEntity updateData = toEntity(request);
        AfterSalesEntity updated = afterSalesService.updateAfterSales(id, updateData);

        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER', 'SERVICE_ENGINEER')")
    @Operation(summary = "Start service", description = "서비스 시작")
    public ResponseEntity<AfterSalesResponse> startService(@PathVariable Long id) {
        log.info("POST /api/after-sales/{}/start", id);

        AfterSalesEntity started = afterSalesService.startService(id);
        return ResponseEntity.ok(toResponse(started));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER', 'SERVICE_ENGINEER')")
    @Operation(summary = "Complete service", description = "서비스 완료")
    public ResponseEntity<AfterSalesResponse> completeService(@PathVariable Long id) {
        log.info("POST /api/after-sales/{}/complete", id);

        AfterSalesEntity completed = afterSalesService.completeService(id);
        return ResponseEntity.ok(toResponse(completed));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE_MANAGER')")
    @Operation(summary = "Close after sales", description = "A/S 종료")
    public ResponseEntity<AfterSalesResponse> closeAfterSales(@PathVariable Long id) {
        log.info("POST /api/after-sales/{}/close", id);

        AfterSalesEntity closed = afterSalesService.closeAfterSales(id);
        return ResponseEntity.ok(toResponse(closed));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete after sales", description = "A/S 삭제")
    public ResponseEntity<Void> deleteAfterSales(@PathVariable Long id) {
        log.info("DELETE /api/after-sales/{}", id);

        afterSalesService.deleteAfterSales(id);
        return ResponseEntity.ok().build();
    }

    // Helper methods for entity-DTO conversion

    private AfterSalesResponse toResponse(AfterSalesEntity entity) {
        return AfterSalesResponse.builder()
                .afterSalesId(entity.getAfterSalesId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .asNo(entity.getAsNo())
                .receiptDate(entity.getReceiptDate())
                .customerId(entity.getCustomer().getCustomerId())
                .customerCode(entity.getCustomerCode())
                .customerName(entity.getCustomerName())
                .contactPerson(entity.getContactPerson())
                .contactPhone(entity.getContactPhone())
                .contactEmail(entity.getContactEmail())
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .serialNo(entity.getSerialNo())
                .lotNo(entity.getLotNo())
                .salesOrderId(entity.getSalesOrder() != null ? entity.getSalesOrder().getSalesOrderId() : null)
                .salesOrderNo(entity.getSalesOrderNo())
                .shippingId(entity.getShipping() != null ? entity.getShipping().getShippingId() : null)
                .purchaseDate(entity.getPurchaseDate())
                .warrantyStatus(entity.getWarrantyStatus())
                .issueCategory(entity.getIssueCategory())
                .issueDescription(entity.getIssueDescription())
                .symptom(entity.getSymptom())
                .serviceType(entity.getServiceType())
                .serviceStatus(entity.getServiceStatus())
                .priority(entity.getPriority())
                .assignedEngineerId(entity.getAssignedEngineer() != null ? entity.getAssignedEngineer().getUserId() : null)
                .assignedEngineerName(entity.getAssignedEngineerName())
                .assignedDate(entity.getAssignedDate())
                .diagnosis(entity.getDiagnosis())
                .serviceAction(entity.getServiceAction())
                .partsReplaced(entity.getPartsReplaced())
                .serviceStartDate(entity.getServiceStartDate())
                .serviceEndDate(entity.getServiceEndDate())
                .serviceCost(entity.getServiceCost())
                .partsCost(entity.getPartsCost())
                .totalCost(entity.getTotalCost())
                .chargeToCustomer(entity.getChargeToCustomer())
                .resolutionDescription(entity.getResolutionDescription())
                .customerSatisfaction(entity.getCustomerSatisfaction())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private AfterSalesEntity toEntity(AfterSalesCreateRequest request) {
        AfterSalesEntity.AfterSalesEntityBuilder builder = AfterSalesEntity.builder()
                .asNo(request.getAsNo())
                .receiptDate(request.getReceiptDate())
                .customer(CustomerEntity.builder().customerId(request.getCustomerId()).build())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .product(ProductEntity.builder().productId(request.getProductId()).build())
                .serialNo(request.getSerialNo())
                .lotNo(request.getLotNo())
                .purchaseDate(request.getPurchaseDate())
                .warrantyStatus(request.getWarrantyStatus())
                .issueCategory(request.getIssueCategory())
                .issueDescription(request.getIssueDescription())
                .symptom(request.getSymptom())
                .serviceType(request.getServiceType())
                .serviceStatus(request.getServiceStatus())
                .priority(request.getPriority())
                .serviceCost(request.getServiceCost())
                .partsCost(request.getPartsCost())
                .chargeToCustomer(request.getChargeToCustomer())
                .remarks(request.getRemarks());

        if (request.getSalesOrderId() != null) {
            builder.salesOrder(SalesOrderEntity.builder().salesOrderId(request.getSalesOrderId()).build());
        }
        if (request.getShippingId() != null) {
            builder.shipping(ShippingEntity.builder().shippingId(request.getShippingId()).build());
        }
        if (request.getAssignedEngineerId() != null) {
            builder.assignedEngineer(UserEntity.builder().userId(request.getAssignedEngineerId()).build());
        }

        return builder.build();
    }

    private AfterSalesEntity toEntity(AfterSalesUpdateRequest request) {
        return AfterSalesEntity.builder()
                .issueCategory(request.getIssueCategory())
                .issueDescription(request.getIssueDescription())
                .symptom(request.getSymptom())
                .serviceType(request.getServiceType())
                .serviceStatus(request.getServiceStatus())
                .priority(request.getPriority())
                .diagnosis(request.getDiagnosis())
                .serviceAction(request.getServiceAction())
                .partsReplaced(request.getPartsReplaced())
                .serviceCost(request.getServiceCost())
                .partsCost(request.getPartsCost())
                .chargeToCustomer(request.getChargeToCustomer())
                .resolutionDescription(request.getResolutionDescription())
                .customerSatisfaction(request.getCustomerSatisfaction())
                .remarks(request.getRemarks())
                .build();
    }
}
