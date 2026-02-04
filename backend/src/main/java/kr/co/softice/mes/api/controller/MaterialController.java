package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.material.MaterialCreateRequest;
import kr.co.softice.mes.common.dto.material.MaterialResponse;
import kr.co.softice.mes.common.dto.material.MaterialUpdateRequest;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.MaterialEntity;
import kr.co.softice.mes.domain.entity.SupplierEntity;
import kr.co.softice.mes.domain.service.MaterialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Material Controller
 * 자재 마스터 컨트롤러
 *
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Material Management", description = "자재 관리 API")
public class MaterialController {

    private final MaterialService materialService;

    /**
     * 모든 자재 조회
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "모든 자재 조회", description = "테넌트의 모든 자재를 조회합니다")
    public ResponseEntity<List<MaterialResponse>> getAllMaterials() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/materials - tenant: {}", tenantId);

        List<MaterialEntity> materials = materialService.getAllMaterials(tenantId);
        List<MaterialResponse> responses = materials.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 활성 자재 조회
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 자재 조회", description = "테넌트의 활성 자재를 조회합니다")
    public ResponseEntity<List<MaterialResponse>> getActiveMaterials() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/materials/active - tenant: {}", tenantId);

        List<MaterialEntity> materials = materialService.getActiveMaterials(tenantId);
        List<MaterialResponse> responses = materials.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 자재 유형별 조회
     */
    @GetMapping("/type/{materialType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "자재 유형별 조회", description = "특정 유형의 자재를 조회합니다")
    public ResponseEntity<List<MaterialResponse>> getMaterialsByType(@PathVariable String materialType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/materials/type/{} - tenant: {}", materialType, tenantId);

        List<MaterialEntity> materials = materialService.getMaterialsByType(tenantId, materialType);
        List<MaterialResponse> responses = materials.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 공급업체별 자재 조회
     */
    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "공급업체별 자재 조회", description = "특정 공급업체의 자재를 조회합니다")
    public ResponseEntity<List<MaterialResponse>> getMaterialsBySupplier(@PathVariable Long supplierId) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/materials/supplier/{} - tenant: {}", supplierId, tenantId);

        List<MaterialEntity> materials = materialService.getMaterialsBySupplier(tenantId, supplierId);
        List<MaterialResponse> responses = materials.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * 자재 ID로 조회
     */
    @GetMapping("/{materialId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "자재 조회", description = "자재 ID로 자재를 조회합니다")
    public ResponseEntity<MaterialResponse> getMaterialById(@PathVariable Long materialId) {
        log.info("GET /api/materials/{}", materialId);

        MaterialEntity material = materialService.getMaterialById(materialId);
        return ResponseEntity.ok(toResponse(material));
    }

    /**
     * 자재 생성
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MATERIAL_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "자재 생성", description = "새로운 자재를 생성합니다")
    public ResponseEntity<MaterialResponse> createMaterial(@Valid @RequestBody MaterialCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/materials - tenant: {}, materialCode: {}", tenantId, request.getMaterialCode());

        MaterialEntity material = toEntity(request);
        MaterialEntity created = materialService.createMaterial(tenantId, material);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    /**
     * 자재 수정
     */
    @PutMapping("/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MATERIAL_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "자재 수정", description = "기존 자재를 수정합니다")
    public ResponseEntity<MaterialResponse> updateMaterial(
            @PathVariable Long materialId,
            @Valid @RequestBody MaterialUpdateRequest request) {
        log.info("PUT /api/materials/{}", materialId);

        MaterialEntity material = toEntity(request);
        MaterialEntity updated = materialService.updateMaterial(materialId, material);

        return ResponseEntity.ok(toResponse(updated));
    }

    /**
     * 자재 삭제
     */
    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MATERIAL_MANAGER')")
    @Operation(summary = "자재 삭제", description = "자재를 삭제합니다")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) {
        log.info("DELETE /api/materials/{}", materialId);

        materialService.deleteMaterial(materialId);
        return ResponseEntity.ok().build();
    }

    /**
     * 자재 활성화/비활성화 토글
     */
    @PostMapping("/{materialId}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MATERIAL_MANAGER', 'PRODUCTION_MANAGER')")
    @Operation(summary = "자재 활성화/비활성화", description = "자재의 활성화 상태를 토글합니다")
    public ResponseEntity<MaterialResponse> toggleActive(@PathVariable Long materialId) {
        log.info("POST /api/materials/{}/toggle-active", materialId);

        MaterialEntity toggled = materialService.toggleActive(materialId);
        return ResponseEntity.ok(toResponse(toggled));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private MaterialResponse toResponse(MaterialEntity entity) {
        return MaterialResponse.builder()
                .materialId(entity.getMaterialId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .materialCode(entity.getMaterialCode())
                .materialName(entity.getMaterialName())
                .materialType(entity.getMaterialType())
                .specification(entity.getSpecification())
                .model(entity.getModel())
                .unit(entity.getUnit())
                .standardPrice(entity.getStandardPrice())
                .currentPrice(entity.getCurrentPrice())
                .currency(entity.getCurrency())
                .supplierId(entity.getSupplier() != null ? entity.getSupplier().getSupplierId() : null)
                .supplierCode(entity.getSupplier() != null ? entity.getSupplier().getSupplierCode() : null)
                .supplierName(entity.getSupplier() != null ? entity.getSupplier().getSupplierName() : null)
                .leadTimeDays(entity.getLeadTimeDays())
                .minStockQuantity(entity.getMinStockQuantity())
                .maxStockQuantity(entity.getMaxStockQuantity())
                .safetyStockQuantity(entity.getSafetyStockQuantity())
                .reorderPoint(entity.getReorderPoint())
                .storageLocation(entity.getStorageLocation())
                .lotManaged(entity.getLotManaged())
                .shelfLifeDays(entity.getShelfLifeDays())
                .isActive(entity.getIsActive())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * CreateRequest를 Entity로 변환
     */
    private MaterialEntity toEntity(MaterialCreateRequest request) {
        MaterialEntity.MaterialEntityBuilder builder = MaterialEntity.builder()
                .materialCode(request.getMaterialCode())
                .materialName(request.getMaterialName())
                .materialType(request.getMaterialType())
                .specification(request.getSpecification())
                .model(request.getModel())
                .unit(request.getUnit())
                .standardPrice(request.getStandardPrice())
                .currentPrice(request.getCurrentPrice())
                .currency(request.getCurrency())
                .leadTimeDays(request.getLeadTimeDays())
                .minStockQuantity(request.getMinStockQuantity())
                .maxStockQuantity(request.getMaxStockQuantity())
                .safetyStockQuantity(request.getSafetyStockQuantity())
                .reorderPoint(request.getReorderPoint())
                .storageLocation(request.getStorageLocation())
                .lotManaged(request.getLotManaged())
                .shelfLifeDays(request.getShelfLifeDays())
                .isActive(request.getIsActive())
                .remarks(request.getRemarks());

        if (request.getSupplierId() != null) {
            builder.supplier(SupplierEntity.builder().supplierId(request.getSupplierId()).build());
        }

        return builder.build();
    }

    /**
     * UpdateRequest를 Entity로 변환
     */
    private MaterialEntity toEntity(MaterialUpdateRequest request) {
        MaterialEntity.MaterialEntityBuilder builder = MaterialEntity.builder()
                .materialName(request.getMaterialName())
                .materialType(request.getMaterialType())
                .specification(request.getSpecification())
                .model(request.getModel())
                .unit(request.getUnit())
                .standardPrice(request.getStandardPrice())
                .currentPrice(request.getCurrentPrice())
                .currency(request.getCurrency())
                .leadTimeDays(request.getLeadTimeDays())
                .minStockQuantity(request.getMinStockQuantity())
                .maxStockQuantity(request.getMaxStockQuantity())
                .safetyStockQuantity(request.getSafetyStockQuantity())
                .reorderPoint(request.getReorderPoint())
                .storageLocation(request.getStorageLocation())
                .lotManaged(request.getLotManaged())
                .shelfLifeDays(request.getShelfLifeDays())
                .isActive(request.getIsActive())
                .remarks(request.getRemarks());

        if (request.getSupplierId() != null) {
            builder.supplier(SupplierEntity.builder().supplierId(request.getSupplierId()).build());
        }

        return builder.build();
    }
}
