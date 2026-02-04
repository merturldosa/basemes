package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.defect.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.service.DefectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Defect Controller
 * 불량 관리 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/defects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Defects", description = "불량 관리 API")
public class DefectController {

    private final DefectService defectService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'QUALITY_USER')")
    @Operation(summary = "Get all defects", description = "모든 불량 조회")
    public ResponseEntity<List<DefectResponse>> getAllDefects() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/defects - tenant: {}", tenantId);

        List<DefectEntity> defects = defectService.getAllDefects(tenantId);
        List<DefectResponse> responses = defects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'QUALITY_USER')")
    @Operation(summary = "Get defect by ID", description = "ID로 불량 조회")
    public ResponseEntity<DefectResponse> getDefectById(@PathVariable Long id) {
        log.info("GET /api/defects/{}", id);

        DefectEntity defect = defectService.getDefectById(id);
        return ResponseEntity.ok(toResponse(defect));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'QUALITY_USER')")
    @Operation(summary = "Get defects by status", description = "상태별 불량 조회")
    public ResponseEntity<List<DefectResponse>> getDefectsByStatus(@PathVariable String status) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/defects/status/{} - tenant: {}", status, tenantId);

        List<DefectEntity> defects = defectService.getDefectsByStatus(tenantId, status);
        List<DefectResponse> responses = defects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/source-type/{sourceType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER', 'QUALITY_USER')")
    @Operation(summary = "Get defects by source type", description = "발생 원천별 불량 조회")
    public ResponseEntity<List<DefectResponse>> getDefectsBySourceType(@PathVariable String sourceType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/defects/source-type/{} - tenant: {}", sourceType, tenantId);

        List<DefectEntity> defects = defectService.getDefectsBySourceType(tenantId, sourceType);
        List<DefectResponse> responses = defects.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "Create defect", description = "불량 생성")
    public ResponseEntity<DefectResponse> createDefect(@Valid @RequestBody DefectCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/defects - tenant: {}, defectNo: {}", tenantId, request.getDefectNo());

        DefectEntity entity = toEntity(request);
        DefectEntity created = defectService.createDefect(tenantId, entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "Update defect", description = "불량 수정")
    public ResponseEntity<DefectResponse> updateDefect(
            @PathVariable Long id,
            @Valid @RequestBody DefectUpdateRequest request) {
        log.info("PUT /api/defects/{}", id);

        DefectEntity updateData = toEntity(request);
        DefectEntity updated = defectService.updateDefect(id, updateData);

        return ResponseEntity.ok(toResponse(updated));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'QUALITY_MANAGER')")
    @Operation(summary = "Close defect", description = "불량 종료")
    public ResponseEntity<DefectResponse> closeDefect(@PathVariable Long id) {
        log.info("POST /api/defects/{}/close", id);

        DefectEntity closed = defectService.closeDefect(id);
        return ResponseEntity.ok(toResponse(closed));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete defect", description = "불량 삭제")
    public ResponseEntity<Void> deleteDefect(@PathVariable Long id) {
        log.info("DELETE /api/defects/{}", id);

        defectService.deleteDefect(id);
        return ResponseEntity.ok().build();
    }

    // Helper methods for entity-DTO conversion

    private DefectResponse toResponse(DefectEntity entity) {
        return DefectResponse.builder()
                .defectId(entity.getDefectId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .defectNo(entity.getDefectNo())
                .defectDate(entity.getDefectDate())
                .sourceType(entity.getSourceType())
                .workOrderId(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderId() : null)
                .workOrderNo(entity.getWorkOrder() != null ? entity.getWorkOrder().getWorkOrderNo() : null)
                .workResultId(entity.getWorkResult() != null ? entity.getWorkResult().getWorkResultId() : null)
                .goodsReceiptId(entity.getGoodsReceipt() != null ? entity.getGoodsReceipt().getGoodsReceiptId() : null)
                .goodsReceiptNo(entity.getGoodsReceipt() != null ? entity.getGoodsReceipt().getReceiptNo() : null)
                .shippingId(entity.getShipping() != null ? entity.getShipping().getShippingId() : null)
                .shippingNo(entity.getShipping() != null ? entity.getShipping().getShippingNo() : null)
                .qualityInspectionId(entity.getQualityInspection() != null ? entity.getQualityInspection().getQualityInspectionId() : null)
                .qualityInspectionNo(entity.getQualityInspection() != null ? entity.getQualityInspection().getInspectionNo() : null)
                .productId(entity.getProduct().getProductId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .defectType(entity.getDefectType())
                .defectCategory(entity.getDefectCategory())
                .defectLocation(entity.getDefectLocation())
                .defectDescription(entity.getDefectDescription())
                .defectQuantity(entity.getDefectQuantity())
                .lotNo(entity.getLotNo())
                .severity(entity.getSeverity())
                .status(entity.getStatus())
                .responsibleDepartmentId(entity.getResponsibleDepartment() != null ? entity.getResponsibleDepartment().getDepartmentId() : null)
                .responsibleDepartmentName(entity.getResponsibleDepartment() != null ? entity.getResponsibleDepartment().getDepartmentName() : null)
                .responsibleUserId(entity.getResponsibleUser() != null ? entity.getResponsibleUser().getUserId() : null)
                .responsibleUserName(entity.getResponsibleUser() != null ? entity.getResponsibleUser().getFullName() : null)
                .rootCause(entity.getRootCause())
                .correctiveAction(entity.getCorrectiveAction())
                .preventiveAction(entity.getPreventiveAction())
                .actionDate(entity.getActionDate())
                .reporterUserId(entity.getReporterUser() != null ? entity.getReporterUser().getUserId() : null)
                .reporterName(entity.getReporterName())
                .defectCost(entity.getDefectCost())
                .remarks(entity.getRemarks())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private DefectEntity toEntity(DefectCreateRequest request) {
        DefectEntity.DefectEntityBuilder builder = DefectEntity.builder()
                .defectNo(request.getDefectNo())
                .defectDate(request.getDefectDate())
                .sourceType(request.getSourceType())
                .product(ProductEntity.builder().productId(request.getProductId()).build())
                .productCode(request.getDefectNo()) // Temporary, will be set by service
                .productName(request.getDefectNo()) // Temporary, will be set by service
                .defectType(request.getDefectType())
                .defectCategory(request.getDefectCategory())
                .defectLocation(request.getDefectLocation())
                .defectDescription(request.getDefectDescription())
                .defectQuantity(request.getDefectQuantity())
                .lotNo(request.getLotNo())
                .severity(request.getSeverity())
                .status(request.getStatus())
                .rootCause(request.getRootCause())
                .correctiveAction(request.getCorrectiveAction())
                .preventiveAction(request.getPreventiveAction())
                .defectCost(request.getDefectCost())
                .remarks(request.getRemarks());

        if (request.getWorkOrderId() != null) {
            builder.workOrder(WorkOrderEntity.builder().workOrderId(request.getWorkOrderId()).build());
        }
        if (request.getWorkResultId() != null) {
            builder.workResult(WorkResultEntity.builder().workResultId(request.getWorkResultId()).build());
        }
        if (request.getGoodsReceiptId() != null) {
            builder.goodsReceipt(GoodsReceiptEntity.builder().goodsReceiptId(request.getGoodsReceiptId()).build());
        }
        if (request.getShippingId() != null) {
            builder.shipping(ShippingEntity.builder().shippingId(request.getShippingId()).build());
        }
        if (request.getQualityInspectionId() != null) {
            builder.qualityInspection(QualityInspectionEntity.builder().qualityInspectionId(request.getQualityInspectionId()).build());
        }
        if (request.getResponsibleDepartmentId() != null) {
            builder.responsibleDepartment(DepartmentEntity.builder().departmentId(request.getResponsibleDepartmentId()).build());
        }
        if (request.getResponsibleUserId() != null) {
            builder.responsibleUser(UserEntity.builder().userId(request.getResponsibleUserId()).build());
        }
        if (request.getReporterUserId() != null) {
            builder.reporterUser(UserEntity.builder().userId(request.getReporterUserId()).build());
        }

        return builder.build();
    }

    private DefectEntity toEntity(DefectUpdateRequest request) {
        return DefectEntity.builder()
                .defectType(request.getDefectType())
                .defectCategory(request.getDefectCategory())
                .defectLocation(request.getDefectLocation())
                .defectDescription(request.getDefectDescription())
                .defectQuantity(request.getDefectQuantity())
                .severity(request.getSeverity())
                .status(request.getStatus())
                .rootCause(request.getRootCause())
                .correctiveAction(request.getCorrectiveAction())
                .preventiveAction(request.getPreventiveAction())
                .defectCost(request.getDefectCost())
                .remarks(request.getRemarks())
                .build();
    }
}
