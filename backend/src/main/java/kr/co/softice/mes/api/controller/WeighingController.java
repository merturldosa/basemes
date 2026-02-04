package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.weighing.*;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.service.WeighingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Weighing Controller
 * 칭량 관리 API
 *
 * 엔드포인트:
 * - GET /api/weighings - 칭량 목록
 * - GET /api/weighings/{id} - 칭량 상세
 * - GET /api/weighings/tolerance-exceeded - 허용 오차 초과 목록
 * - GET /api/weighings/pending-verification - 검증 대기 목록
 * - GET /api/weighings/reference/{type}/{id} - 참조 문서별 조회
 * - POST /api/weighings - 칭량 생성
 * - PUT /api/weighings/{id} - 칭량 수정
 * - POST /api/weighings/{id}/verify - 칭량 검증 (GMP 이중 검증)
 * - DELETE /api/weighings/{id} - 칭량 삭제
 *
 * 워크플로우: PENDING → VERIFIED or REJECTED
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/weighings")
@RequiredArgsConstructor
@Tag(name = "Weighing Management", description = "칭량 관리 API (GMP 준수)")
public class WeighingController {

    private final WeighingService weighingService;

    /**
     * 칭량 목록 조회
     * GET /api/weighings
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 목록 조회", description = "테넌트의 모든 칭량 기록 조회")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getWeighings(
            @RequestParam(required = false) String weighingType,
            @RequestParam(required = false) String verificationStatus,
            @RequestParam(required = false) Long productId) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting weighings for tenant: {}, type: {}, status: {}, product: {}",
            tenantId, weighingType, verificationStatus, productId);

        // Get all weighings and filter in controller
        // Note: For better performance, consider adding specific query methods to repository
        List<WeighingResponse> responses = weighingService.getAllWeighings(tenantId);

        // Filter by type if specified
        if (weighingType != null) {
            responses = responses.stream()
                .filter(w -> weighingType.equals(w.getWeighingType()))
                .collect(Collectors.toList());
        }

        // Filter by verification status if specified
        if (verificationStatus != null) {
            responses = responses.stream()
                .filter(w -> verificationStatus.equals(w.getVerificationStatus()))
                .collect(Collectors.toList());
        }

        // Filter by product if specified
        if (productId != null) {
            responses = responses.stream()
                .filter(w -> w.getProductId().equals(productId))
                .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success("칭량 목록 조회 성공", responses));
    }

    /**
     * 칭량 상세 조회
     * GET /api/weighings/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 상세 조회", description = "칭량 ID로 상세 정보 조회")
    public ResponseEntity<ApiResponse<WeighingResponse>> getWeighing(@PathVariable Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting weighing: {} for tenant: {}", id, tenantId);

        WeighingResponse response = weighingService.getWeighingById(tenantId, id);

        return ResponseEntity.ok(ApiResponse.success("칭량 조회 성공", response));
    }

    /**
     * 허용 오차 초과 칭량 조회
     * GET /api/weighings/tolerance-exceeded
     */
    @GetMapping("/tolerance-exceeded")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "허용 오차 초과 조회", description = "허용 오차를 초과한 칭량 기록 조회")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getToleranceExceeded() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting tolerance exceeded weighings for tenant: {}", tenantId);

        List<WeighingResponse> responses = weighingService.getToleranceExceededWeighings(tenantId);

        return ResponseEntity.ok(ApiResponse.success("허용 오차 초과 칭량 조회 성공", responses));
    }

    /**
     * 검증 대기 칭량 조회
     * GET /api/weighings/pending-verification
     */
    @GetMapping("/pending-verification")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "검증 대기 조회", description = "검증 대기 중인 칭량 기록 조회")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getPendingVerification() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting pending verification weighings for tenant: {}", tenantId);

        List<WeighingResponse> responses = weighingService.getPendingVerificationWeighings(tenantId);

        return ResponseEntity.ok(ApiResponse.success("검증 대기 칭량 조회 성공", responses));
    }

    /**
     * 미검증 허용 오차 초과 칭량 조회 (긴급 주의 필요)
     * GET /api/weighings/unverified-tolerance-exceeded
     */
    @GetMapping("/unverified-tolerance-exceeded")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "미검증 허용 오차 초과 조회", description = "검증되지 않은 허용 오차 초과 칭량 (긴급 주의)")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getUnverifiedToleranceExceeded() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting unverified tolerance exceeded weighings for tenant: {}", tenantId);

        // Get tolerance exceeded weighings and filter for pending verification status
        List<WeighingResponse> responses = weighingService.getToleranceExceededWeighings(tenantId).stream()
                .filter(w -> "PENDING".equals(w.getVerificationStatus()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("미검증 허용 오차 초과 칭량 조회 성공", responses));
    }

    /**
     * 참조 문서별 칭량 조회
     * GET /api/weighings/reference/{type}/{id}
     */
    @GetMapping("/reference/{type}/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "참조 문서별 조회", description = "참조 문서(불출, 작업지시 등)의 칭량 기록 조회")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getWeighingsByReference(
            @PathVariable String type,
            @PathVariable Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting weighings for tenant: {}, reference: type={}, id={}", tenantId, type, id);

        List<WeighingResponse> responses = weighingService.getWeighingsByReference(tenantId, type, id);

        return ResponseEntity.ok(ApiResponse.success("참조 문서 칭량 조회 성공", responses));
    }

    /**
     * 칭량 생성
     * POST /api/weighings
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 생성", description = "새로운 칭량 기록 생성 (자동 계산)")
    public ResponseEntity<ApiResponse<WeighingResponse>> createWeighing(
            @Valid @RequestBody WeighingCreateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating weighing for tenant: {}, type: {}, product: {}",
            tenantId, request.getWeighingType(), request.getProductId());

        // Create weighing using service (with auto-calculation)
        WeighingResponse response = weighingService.createWeighing(tenantId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("칭량 생성 성공", response));
    }

    /**
     * 칭량 수정
     * PUT /api/weighings/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 수정", description = "칭량 기록 수정 (PENDING 상태만 가능)")
    public ResponseEntity<ApiResponse<WeighingResponse>> updateWeighing(
            @PathVariable Long id,
            @Valid @RequestBody WeighingUpdateRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Updating weighing: {} for tenant: {}", id, tenantId);

        // Update weighing using service
        WeighingResponse response = weighingService.updateWeighing(tenantId, id, request);

        return ResponseEntity.ok(ApiResponse.success("칭량 수정 성공", response));
    }

    /**
     * 칭량 검증 또는 거부
     * POST /api/weighings/{id}/verify
     */
    @PostMapping("/{id}/verify")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "칭량 검증/거부", description = "칭량 검증 또는 거부 (GMP 이중 검증)")
    public ResponseEntity<ApiResponse<WeighingResponse>> verifyWeighing(
            @PathVariable Long id,
            @Valid @RequestBody WeighingVerificationRequest request) {

        String tenantId = TenantContext.getCurrentTenant();
        log.info("Verifying weighing: {} with action: {} for tenant: {}", id, request.getAction(), tenantId);

        // Verify or reject using service
        WeighingResponse response = weighingService.verifyWeighing(tenantId, id, request);

        String message = "VERIFY".equals(request.getAction()) ? "칭량 검증 성공" : "칭량 거부 성공";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    /**
     * 칭량 삭제
     * DELETE /api/weighings/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "칭량 삭제", description = "칭량 삭제 (PENDING 또는 REJECTED만 가능, 관리자 권한)")
    public ResponseEntity<ApiResponse<Void>> deleteWeighing(@PathVariable Long id) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Deleting weighing: {} for tenant: {}", id, tenantId);

        weighingService.deleteWeighing(tenantId, id);

        return ResponseEntity.ok(ApiResponse.success("칭량 삭제 성공", null));
    }

}
