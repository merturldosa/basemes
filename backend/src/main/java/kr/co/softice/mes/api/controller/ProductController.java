package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.product.ProductCreateRequest;
import kr.co.softice.mes.common.dto.product.ProductResponse;
import kr.co.softice.mes.common.dto.product.ProductUpdateRequest;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product Controller
 * 제품 마스터 관리 API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Product Management", description = "제품 마스터 관리 API")
public class ProductController {

    private final ProductService productService;
    private final TenantRepository tenantRepository;

    /**
     * 제품 목록 조회
     * GET /api/products
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "제품 목록 조회", description = "테넌트의 모든 제품 조회")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting products for tenant: {}", tenantId);

        List<ProductResponse> products = productService.findByTenant(tenantId).stream()
                .map(this::toProductResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("제품 목록 조회 성공", products));
    }

    /**
     * 활성 제품 목록 조회
     * GET /api/products/active
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 제품 목록 조회", description = "활성 상태의 제품만 조회")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getActiveProducts() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active products for tenant: {}", tenantId);

        List<ProductResponse> products = productService.findActiveByTenant(tenantId).stream()
                .map(this::toProductResponse)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("활성 제품 목록 조회 성공", products));
    }

    /**
     * 제품 상세 조회
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "제품 상세 조회", description = "제품 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        log.info("Getting product: {}", id);

        ProductEntity product = productService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("제품 조회 성공", toProductResponse(product)));
    }

    /**
     * 제품 코드로 조회
     * GET /api/products/code/{productCode}
     */
    @GetMapping("/code/{productCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "제품 코드로 조회", description = "제품 코드로 제품 정보 조회")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByCode(@PathVariable String productCode) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting product by code: {} for tenant: {}", productCode, tenantId);

        ProductEntity product = productService.findByProductCode(tenantId, productCode)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        return ResponseEntity.ok(ApiResponse.success("제품 조회 성공", toProductResponse(product)));
    }

    /**
     * 제품 생성
     * POST /api/products
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "제품 생성", description = "신규 제품 등록")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating product: {} for tenant: {}", request.getProductCode(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        ProductEntity product = ProductEntity.builder()
                .tenant(tenant)
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .productType(request.getProductType())
                .specification(request.getSpecification())
                .unit(request.getUnit())
                .standardCycleTime(request.getStandardCycleTime() != null ? request.getStandardCycleTime().intValue() : null)
                .isActive(true)
                .description(request.getRemarks())
                .build();

        ProductEntity createdProduct = productService.createProduct(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("제품 생성 성공", toProductResponse(createdProduct)));
    }

    /**
     * 제품 수정
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "제품 수정", description = "제품 정보 수정")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {

        log.info("Updating product: {}", id);

        ProductEntity product = productService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getProductType() != null) {
            product.setProductType(request.getProductType());
        }
        if (request.getSpecification() != null) {
            product.setSpecification(request.getSpecification());
        }
        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }
        if (request.getStandardCycleTime() != null) {
            product.setStandardCycleTime(request.getStandardCycleTime().intValue());
        }
        if (request.getRemarks() != null) {
            product.setDescription(request.getRemarks());
        }

        ProductEntity updatedProduct = productService.updateProduct(product);

        return ResponseEntity.ok(ApiResponse.success("제품 수정 성공", toProductResponse(updatedProduct)));
    }

    /**
     * 제품 삭제
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "제품 삭제", description = "제품 완전 삭제 (관리자만 가능)")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product: {}", id);

        productService.deleteProduct(id);

        return ResponseEntity.ok(ApiResponse.success("제품 삭제 성공", null));
    }

    /**
     * 제품 활성화
     * POST /api/products/{id}/activate
     */
    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "제품 활성화", description = "제품을 활성 상태로 변경")
    public ResponseEntity<ApiResponse<ProductResponse>> activateProduct(@PathVariable Long id) {
        log.info("Activating product: {}", id);

        ProductEntity product = productService.activateProduct(id);

        return ResponseEntity.ok(ApiResponse.success("제품 활성화 성공", toProductResponse(product)));
    }

    /**
     * 제품 비활성화
     * POST /api/products/{id}/deactivate
     */
    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PRODUCTION_MANAGER')")
    @Operation(summary = "제품 비활성화", description = "제품을 비활성 상태로 변경")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivateProduct(@PathVariable Long id) {
        log.info("Deactivating product: {}", id);

        ProductEntity product = productService.deactivateProduct(id);

        return ResponseEntity.ok(ApiResponse.success("제품 비활성화 성공", toProductResponse(product)));
    }

    /**
     * Entity를 Response DTO로 변환
     */
    private ProductResponse toProductResponse(ProductEntity product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .productType(product.getProductType())
                .specification(product.getSpecification())
                .unit(product.getUnit())
                .standardCycleTime(product.getStandardCycleTime() != null ? BigDecimal.valueOf(product.getStandardCycleTime()) : null)
                .isActive(product.getIsActive())
                .tenantId(product.getTenant().getTenantId())
                .tenantName(product.getTenant().getTenantName())
                .remarks(product.getDescription())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
