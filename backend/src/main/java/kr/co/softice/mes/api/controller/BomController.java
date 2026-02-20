package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.bom.*;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.ProcessRepository;
import kr.co.softice.mes.domain.service.BomService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BOM Controller
 * BOM 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
@Tag(name = "BOM", description = "BOM 관리 API")
public class BomController {

    private final BomService bomService;
    private final ProductRepository productRepository;
    private final ProcessRepository processRepository;
    private final kr.co.softice.mes.domain.repository.TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER', 'USER')")
    public ResponseEntity<List<BomResponse>> getAllBoms() {
        String tenantId = TenantContext.getCurrentTenant();
        List<BomEntity> boms = bomService.findByTenant(tenantId);
        return ResponseEntity.ok(boms.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER', 'USER')")
    public ResponseEntity<List<BomResponse>> getActiveBoms() {
        String tenantId = TenantContext.getCurrentTenant();
        List<BomEntity> boms = bomService.findActiveByTenant(tenantId);
        return ResponseEntity.ok(boms.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER', 'USER')")
    public ResponseEntity<List<BomResponse>> getBomsByProduct(@PathVariable Long productId) {
        String tenantId = TenantContext.getCurrentTenant();
        List<BomEntity> boms = bomService.findByTenantAndProduct(tenantId, productId);
        return ResponseEntity.ok(boms.stream()
            .map(this::toResponse)
            .collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    @GetMapping("/{bomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER', 'USER')")
    public ResponseEntity<BomResponse> getBomById(@PathVariable Long bomId) {
        return bomService.findById(bomId)
            .map(bom -> ResponseEntity.ok(toResponse(bom)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER')")
    public ResponseEntity<BomResponse> createBom(@Valid @RequestBody BomCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        ProductEntity product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        BomEntity bom = BomEntity.builder()
            .tenant(tenant)
            .product(product)
            .bomCode(request.getBomCode())
            .bomName(request.getBomName())
            .version(request.getVersion())
            .effectiveDate(request.getEffectiveDate())
            .expiryDate(request.getExpiryDate())
            .isActive(request.getIsActive())
            .remarks(request.getRemarks())
            .details(new ArrayList<>())
            .build();

        // Add details
        for (BomDetailRequest detailReq : request.getDetails()) {
            ProductEntity materialProduct = productRepository.findById(detailReq.getMaterialProductId())
                .orElseThrow(() -> new IllegalArgumentException("Material product not found"));

            ProcessEntity process = null;
            if (detailReq.getProcessId() != null) {
                process = processRepository.findById(detailReq.getProcessId())
                    .orElseThrow(() -> new IllegalArgumentException("Process not found"));
            }

            BomDetailEntity detail = BomDetailEntity.builder()
                .sequence(detailReq.getSequence())
                .materialProduct(materialProduct)
                .process(process)
                .quantity(detailReq.getQuantity())
                .unit(detailReq.getUnit())
                .usageRate(detailReq.getUsageRate())
                .scrapRate(detailReq.getScrapRate())
                .remarks(detailReq.getRemarks())
                .build();

            bom.addDetail(detail);
        }

        BomEntity created = bomService.createBom(bom);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{bomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER')")
    public ResponseEntity<BomResponse> updateBom(
        @PathVariable Long bomId,
        @Valid @RequestBody BomUpdateRequest request) {

        return bomService.findById(bomId)
            .map(bom -> {
                bom.setBomName(request.getBomName());
                bom.setEffectiveDate(request.getEffectiveDate());
                bom.setExpiryDate(request.getExpiryDate());
                bom.setIsActive(request.getIsActive());
                bom.setRemarks(request.getRemarks());

                // Clear and rebuild details
                bom.clearDetails();
                for (BomDetailRequest detailReq : request.getDetails()) {
                    ProductEntity materialProduct = productRepository.findById(detailReq.getMaterialProductId())
                        .orElseThrow(() -> new IllegalArgumentException("Material product not found"));

                    ProcessEntity process = null;
                    if (detailReq.getProcessId() != null) {
                        process = processRepository.findById(detailReq.getProcessId())
                            .orElseThrow(() -> new IllegalArgumentException("Process not found"));
                    }

                    BomDetailEntity detail = BomDetailEntity.builder()
                        .sequence(detailReq.getSequence())
                        .materialProduct(materialProduct)
                        .process(process)
                        .quantity(detailReq.getQuantity())
                        .unit(detailReq.getUnit())
                        .usageRate(detailReq.getUsageRate())
                        .scrapRate(detailReq.getScrapRate())
                        .remarks(detailReq.getRemarks())
                        .build();

                    bom.addDetail(detail);
                }

                BomEntity updated = bomService.updateBom(bom);
                return ResponseEntity.ok(toResponse(updated));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{bomId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBom(@PathVariable Long bomId) {
        bomService.deleteBom(bomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bomId}/toggle-active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER')")
    public ResponseEntity<BomResponse> toggleActive(@PathVariable Long bomId) {
        BomEntity bom = bomService.toggleActive(bomId);
        return ResponseEntity.ok(toResponse(bom));
    }

    @PostMapping("/{bomId}/copy")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER', 'ENGINEER')")
    public ResponseEntity<BomResponse> copyBom(
        @PathVariable Long bomId,
        @RequestParam String newVersion) {
        BomEntity copied = bomService.copyBom(bomId, newVersion);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(copied));
    }

    private BomResponse toResponse(BomEntity bom) {
        List<BomDetailResponse> detailResponses = bom.getDetails() != null
            ? bom.getDetails().stream()
                .map(this::toDetailResponse)
                .collect(Collectors.toList())
            : new ArrayList<>();

        return BomResponse.builder()
            .bomId(bom.getBomId())
            .tenantId(bom.getTenant().getTenantId())
            .tenantName(bom.getTenant().getTenantName())
            .productId(bom.getProduct().getProductId())
            .productCode(bom.getProduct().getProductCode())
            .productName(bom.getProduct().getProductName())
            .bomCode(bom.getBomCode())
            .bomName(bom.getBomName())
            .version(bom.getVersion())
            .effectiveDate(bom.getEffectiveDate())
            .expiryDate(bom.getExpiryDate())
            .isActive(bom.getIsActive())
            .remarks(bom.getRemarks())
            .details(detailResponses)
            .createdAt(bom.getCreatedAt())
            .updatedAt(bom.getUpdatedAt())
            .build();
    }

    private BomDetailResponse toDetailResponse(BomDetailEntity detail) {
        return BomDetailResponse.builder()
            .bomDetailId(detail.getBomDetailId())
            .bomId(detail.getBom().getBomId())
            .sequence(detail.getSequence())
            .materialProductId(detail.getMaterialProduct().getProductId())
            .materialProductCode(detail.getMaterialProduct().getProductCode())
            .materialProductName(detail.getMaterialProduct().getProductName())
            .processId(detail.getProcess() != null ? detail.getProcess().getProcessId() : null)
            .processCode(detail.getProcess() != null ? detail.getProcess().getProcessCode() : null)
            .processName(detail.getProcess() != null ? detail.getProcess().getProcessName() : null)
            .quantity(detail.getQuantity())
            .unit(detail.getUnit())
            .usageRate(detail.getUsageRate())
            .scrapRate(detail.getScrapRate())
            .remarks(detail.getRemarks())
            .createdAt(detail.getCreatedAt())
            .updatedAt(detail.getUpdatedAt())
            .build();
    }
}
