package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.common.SiteRequest;
import kr.co.softice.mes.common.dto.common.SiteResponse;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.SiteEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.service.SiteService;
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
 * Site Controller
 * 사업장 컨트롤러
 *
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sites", description = "사업장 관리 API")
public class SiteController {

    private final SiteService siteService;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all sites", description = "모든 사업장 조회")
    public ResponseEntity<List<SiteResponse>> getAllSites() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/sites - tenant: {}", tenantId);

        List<SiteEntity> sites = siteService.getAllSitesByTenant(tenantId);
        List<SiteResponse> responses = sites.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get site by ID", description = "ID로 사업장 조회")
    public ResponseEntity<SiteResponse> getSiteById(@PathVariable Long id) {
        log.info("GET /api/sites/{}", id);

        SiteEntity site = siteService.getSiteById(id);
        return ResponseEntity.ok(toResponse(site));
    }

    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    @Operation(summary = "Get active sites", description = "활성 사업장 조회")
    public ResponseEntity<List<SiteResponse>> getActiveSites() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/sites/active - tenant: {}", tenantId);

        List<SiteEntity> sites = siteService.getActiveSites(tenantId);
        List<SiteResponse> responses = sites.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @Transactional(readOnly = true)
    @GetMapping("/type/{siteType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get sites by type", description = "유형별 사업장 조회")
    public ResponseEntity<List<SiteResponse>> getSitesByType(@PathVariable String siteType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("GET /api/sites/type/{} - tenant: {}", siteType, tenantId);

        List<SiteEntity> sites = siteService.getSitesByType(tenantId, siteType);
        List<SiteResponse> responses = sites.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create site", description = "사업장 생성")
    public ResponseEntity<SiteResponse> createSite(@Valid @RequestBody SiteRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("POST /api/sites - tenant: {}, siteCode: {}", tenantId, request.getSiteCode());

        SiteEntity site = toEntity(request, tenantId);
        SiteEntity created = siteService.createSite(site);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update site", description = "사업장 수정")
    public ResponseEntity<SiteResponse> updateSite(
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest request) {
        log.info("PUT /api/sites/{}", id);

        String tenantId = TenantContext.getCurrentTenant();
        SiteEntity site = toEntity(request, tenantId);
        SiteEntity updated = siteService.updateSite(id, site);

        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete site", description = "사업장 삭제")
    public ResponseEntity<Void> deleteSite(@PathVariable Long id) {
        log.info("DELETE /api/sites/{}", id);

        siteService.deleteSite(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle site active status", description = "사업장 활성화 상태 토글")
    public ResponseEntity<SiteResponse> toggleActive(@PathVariable Long id) {
        log.info("POST /api/sites/{}/toggle-active", id);

        SiteEntity toggled = siteService.toggleActive(id);
        return ResponseEntity.ok(toResponse(toggled));
    }

    // Helper methods

    private SiteResponse toResponse(SiteEntity entity) {
        return SiteResponse.builder()
                .siteId(entity.getSiteId())
                .tenantId(entity.getTenant().getTenantId())
                .siteCode(entity.getSiteCode())
                .siteName(entity.getSiteName())
                .address(entity.getAddress())
                .postalCode(entity.getPostalCode())
                .country(entity.getCountry())
                .region(entity.getRegion())
                .phone(entity.getPhone())
                .fax(entity.getFax())
                .email(entity.getEmail())
                .managerName(entity.getManagerName())
                .managerPhone(entity.getManagerPhone())
                .managerEmail(entity.getManagerEmail())
                .siteType(entity.getSiteType())
                .isActive(entity.getIsActive())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private SiteEntity toEntity(SiteRequest request, String tenantId) {
        TenantEntity tenant = new TenantEntity();
        tenant.setTenantId(tenantId);

        return SiteEntity.builder()
                .tenant(tenant)
                .siteCode(request.getSiteCode())
                .siteName(request.getSiteName())
                .address(request.getAddress())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .region(request.getRegion())
                .phone(request.getPhone())
                .fax(request.getFax())
                .email(request.getEmail())
                .managerName(request.getManagerName())
                .managerPhone(request.getManagerPhone())
                .managerEmail(request.getManagerEmail())
                .siteType(request.getSiteType())
                .remarks(request.getRemarks())
                .build();
    }
}
