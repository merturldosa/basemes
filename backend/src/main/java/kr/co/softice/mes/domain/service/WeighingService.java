package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Weighing Service
 * 칭량 관리 서비스
 *
 * 핵심 기능:
 * - 칭량 기록 생성 (자동 계산)
 * - GMP 이중 검증 워크플로우
 * - 허용 오차 모니터링
 * - 참조 문서 연계 (불출, 작업지시, 입고, 출하, 검사)
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeighingService {

    private final WeighingRepository weighingRepository;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;

    /**
     * Find all weighings by tenant
     */
    public List<WeighingEntity> findByTenant(String tenantId) {
        return weighingRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find weighing by ID
     */
    public Optional<WeighingEntity> findById(Long weighingId) {
        return weighingRepository.findByIdWithAllRelations(weighingId);
    }

    /**
     * Find weighings by type
     */
    public List<WeighingEntity> findByType(String tenantId, String weighingType) {
        return weighingRepository.findByTenant_TenantIdAndWeighingType(tenantId, weighingType);
    }

    /**
     * Find weighings by verification status
     */
    public List<WeighingEntity> findByVerificationStatus(String tenantId, String verificationStatus) {
        return weighingRepository.findByVerificationStatusAndTenantWithRelations(tenantId, verificationStatus);
    }

    /**
     * Find weighings by reference (polymorphic lookup)
     */
    public List<WeighingEntity> findByReference(String referenceType, Long referenceId) {
        return weighingRepository.findByReferenceTypeAndReferenceIdWithRelations(referenceType, referenceId);
    }

    /**
     * Find tolerance exceeded weighings
     */
    public List<WeighingEntity> findToleranceExceeded(String tenantId) {
        return weighingRepository.findToleranceExceededWeighings(tenantId);
    }

    /**
     * Find pending verification weighings
     */
    public List<WeighingEntity> findPendingVerification(String tenantId) {
        return weighingRepository.findPendingVerificationWeighings(tenantId);
    }

    /**
     * Find unverified tolerance exceeded weighings (requires immediate attention)
     */
    public List<WeighingEntity> findUnverifiedToleranceExceeded(String tenantId) {
        return weighingRepository.findUnverifiedToleranceExceeded(tenantId);
    }

    /**
     * Create new weighing record
     *
     * 워크플로우:
     * 1. 칭량 번호 자동 생성 (WG-YYYYMMDD-0001)
     * 2. 순중량 계산 (gross - tare)
     * 3. 편차 계산 (net - expected)
     * 4. 허용 오차 확인
     * 5. 저장 (PENDING 상태)
     */
    @Transactional
    public WeighingEntity createWeighing(WeighingEntity weighing) {
        log.info("Creating weighing for tenant: {}, type: {}, product: {}",
            weighing.getTenant().getTenantId(),
            weighing.getWeighingType(),
            weighing.getProduct().getProductCode());

        // 1. Generate weighing number if not provided
        if (weighing.getWeighingNo() == null || weighing.getWeighingNo().isEmpty()) {
            weighing.setWeighingNo(generateWeighingNo(weighing.getTenant().getTenantId()));
        }

        // Check duplicate
        if (weighingRepository.existsByTenant_TenantIdAndWeighingNo(
                weighing.getTenant().getTenantId(), weighing.getWeighingNo())) {
            throw new IllegalArgumentException("Weighing number already exists: " + weighing.getWeighingNo());
        }

        // 2. Perform all calculations
        weighing.performCalculations();

        // 3. Set initial status if not provided
        if (weighing.getVerificationStatus() == null || weighing.getVerificationStatus().isEmpty()) {
            weighing.setVerificationStatus("PENDING");
        }

        // 4. Save weighing
        WeighingEntity saved = weighingRepository.save(weighing);

        // 5. Log tolerance exceeded warning
        if (saved.getToleranceExceeded()) {
            log.warn("Tolerance exceeded for weighing: {}, variance: {}%, tolerance: {}%",
                saved.getWeighingNo(),
                saved.getVariancePercentage(),
                saved.getTolerancePercentage());
        }

        log.info("Created weighing: {} with net weight: {} {}, variance: {}",
            saved.getWeighingNo(),
            saved.getNetWeight(),
            saved.getUnit(),
            saved.getVariance());

        return weighingRepository.findByIdWithAllRelations(saved.getWeighingId())
            .orElse(saved);
    }

    /**
     * Update weighing record
     *
     * Only PENDING weighings can be updated
     */
    @Transactional
    public WeighingEntity updateWeighing(Long weighingId, WeighingEntity updates) {
        log.info("Updating weighing: {}", weighingId);

        WeighingEntity existing = weighingRepository.findById(weighingId)
            .orElseThrow(() -> new IllegalArgumentException("Weighing not found: " + weighingId));

        // Only PENDING weighings can be updated
        if (!"PENDING".equals(existing.getVerificationStatus())) {
            throw new IllegalStateException("Cannot update verified or rejected weighing: " + weighingId);
        }

        // Update fields
        if (updates.getWeighingDate() != null) {
            existing.setWeighingDate(updates.getWeighingDate());
        }
        if (updates.getWeighingType() != null) {
            existing.setWeighingType(updates.getWeighingType());
        }
        if (updates.getReferenceType() != null) {
            existing.setReferenceType(updates.getReferenceType());
        }
        if (updates.getReferenceId() != null) {
            existing.setReferenceId(updates.getReferenceId());
        }
        if (updates.getProduct() != null) {
            existing.setProduct(updates.getProduct());
        }
        if (updates.getLot() != null) {
            existing.setLot(updates.getLot());
        }
        if (updates.getTareWeight() != null) {
            existing.setTareWeight(updates.getTareWeight());
        }
        if (updates.getGrossWeight() != null) {
            existing.setGrossWeight(updates.getGrossWeight());
        }
        if (updates.getExpectedWeight() != null) {
            existing.setExpectedWeight(updates.getExpectedWeight());
        }
        if (updates.getUnit() != null) {
            existing.setUnit(updates.getUnit());
        }
        if (updates.getScaleId() != null) {
            existing.setScaleId(updates.getScaleId());
        }
        if (updates.getScaleName() != null) {
            existing.setScaleName(updates.getScaleName());
        }
        if (updates.getTolerancePercentage() != null) {
            existing.setTolerancePercentage(updates.getTolerancePercentage());
        }
        if (updates.getRemarks() != null) {
            existing.setRemarks(updates.getRemarks());
        }
        if (updates.getAttachments() != null) {
            existing.setAttachments(updates.getAttachments());
        }
        if (updates.getTemperature() != null) {
            existing.setTemperature(updates.getTemperature());
        }
        if (updates.getHumidity() != null) {
            existing.setHumidity(updates.getHumidity());
        }

        // Recalculate all values
        existing.performCalculations();

        WeighingEntity saved = weighingRepository.save(existing);

        log.info("Updated weighing: {}", saved.getWeighingNo());

        return weighingRepository.findByIdWithAllRelations(saved.getWeighingId())
            .orElse(saved);
    }

    /**
     * Verify weighing (GMP dual verification)
     *
     * 워크플로우:
     * 1. 상태 검증 (PENDING만 검증 가능)
     * 2. 검증자 != 작업자 확인 (GMP 요구사항)
     * 3. 검증 정보 업데이트
     * 4. 상태 → VERIFIED
     */
    @Transactional
    public WeighingEntity verifyWeighing(Long weighingId, Long verifierId, String remarks) {
        log.info("Verifying weighing: {} by user: {}", weighingId, verifierId);

        WeighingEntity weighing = weighingRepository.findByIdWithAllRelations(weighingId)
            .orElseThrow(() -> new IllegalArgumentException("Weighing not found: " + weighingId));

        // Only PENDING weighings can be verified
        if (!"PENDING".equals(weighing.getVerificationStatus())) {
            throw new IllegalStateException("Weighing is not pending verification: " + weighingId);
        }

        // GMP requirement: Verifier must be different from operator
        if (weighing.getOperator().getUserId().equals(verifierId)) {
            throw new IllegalArgumentException("Verifier cannot be the same as operator (GMP dual verification requirement)");
        }

        // Get verifier user
        UserEntity verifier = userRepository.findById(verifierId)
            .orElseThrow(() -> new IllegalArgumentException("Verifier user not found: " + verifierId));

        // Verify weighing
        weighing.verify(verifier, remarks);

        WeighingEntity saved = weighingRepository.save(weighing);

        log.info("Verified weighing: {} by user: {}", saved.getWeighingNo(), verifier.getUsername());

        return weighingRepository.findByIdWithAllRelations(saved.getWeighingId())
            .orElse(saved);
    }

    /**
     * Reject weighing
     *
     * 워크플로우:
     * 1. 상태 검증 (PENDING만 거부 가능)
     * 2. 거부 정보 업데이트
     * 3. 상태 → REJECTED
     * 4. 재측정 필요 플래그
     */
    @Transactional
    public WeighingEntity rejectWeighing(Long weighingId, Long verifierId, String rejectionReason) {
        log.info("Rejecting weighing: {} by user: {}", weighingId, verifierId);

        WeighingEntity weighing = weighingRepository.findByIdWithAllRelations(weighingId)
            .orElseThrow(() -> new IllegalArgumentException("Weighing not found: " + weighingId));

        // Only PENDING weighings can be rejected
        if (!"PENDING".equals(weighing.getVerificationStatus())) {
            throw new IllegalStateException("Weighing is not pending verification: " + weighingId);
        }

        // Get verifier user
        UserEntity verifier = userRepository.findById(verifierId)
            .orElseThrow(() -> new IllegalArgumentException("Verifier user not found: " + verifierId));

        // Reject weighing
        weighing.reject(verifier, rejectionReason);

        WeighingEntity saved = weighingRepository.save(weighing);

        log.warn("Rejected weighing: {} by user: {} - Reason: {}",
            saved.getWeighingNo(), verifier.getUsername(), rejectionReason);

        return weighingRepository.findByIdWithAllRelations(saved.getWeighingId())
            .orElse(saved);
    }

    /**
     * Delete weighing
     *
     * Only PENDING or REJECTED weighings can be deleted
     */
    @Transactional
    public void deleteWeighing(Long weighingId) {
        log.info("Deleting weighing: {}", weighingId);

        WeighingEntity weighing = weighingRepository.findById(weighingId)
            .orElseThrow(() -> new IllegalArgumentException("Weighing not found: " + weighingId));

        // Only PENDING or REJECTED weighings can be deleted
        if ("VERIFIED".equals(weighing.getVerificationStatus())) {
            throw new IllegalStateException("Cannot delete verified weighing: " + weighingId);
        }

        weighingRepository.delete(weighing);

        log.info("Deleted weighing: {}", weighing.getWeighingNo());
    }

    /**
     * Generate weighing number: WG-YYYYMMDD-0001
     */
    private String generateWeighingNo(String tenantId) {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "WG-" + dateStr + "-";

        // Find last weighing number for today
        List<WeighingEntity> todayWeighings = weighingRepository.findByTenant_TenantId(tenantId);

        int maxSeq = todayWeighings.stream()
            .map(WeighingEntity::getWeighingNo)
            .filter(no -> no.startsWith(prefix))
            .map(no -> no.substring(prefix.length()))
            .mapToInt(seq -> {
                try {
                    return Integer.parseInt(seq);
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .max()
            .orElse(0);

        return String.format("%s%04d", prefix, maxSeq + 1);
    }

    /**
     * Create weighing from reference (helper method for other services)
     *
     * This method can be called by MaterialRequestService, WorkOrderService, etc.
     * to automatically create weighing records during their workflows
     */
    @Transactional
    public WeighingEntity createWeighingFromReference(
            String tenantId,
            String weighingType,
            String referenceType,
            Long referenceId,
            Long productId,
            Long lotId,
            java.math.BigDecimal tareWeight,
            java.math.BigDecimal grossWeight,
            java.math.BigDecimal expectedWeight,
            Long operatorUserId,
            String remarks) {

        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        ProductEntity product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        UserEntity operator = userRepository.findById(operatorUserId)
            .orElseThrow(() -> new IllegalArgumentException("Operator not found: " + operatorUserId));

        LotEntity lot = null;
        if (lotId != null) {
            lot = lotRepository.findById(lotId)
                .orElse(null);
        }

        WeighingEntity weighing = WeighingEntity.builder()
                .tenant(tenant)
                .weighingDate(LocalDateTime.now())
                .weighingType(weighingType)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .product(product)
                .lot(lot)
                .tareWeight(tareWeight)
                .grossWeight(grossWeight)
                .expectedWeight(expectedWeight)
                .operator(operator)
                .remarks(remarks)
                .build();

        return createWeighing(weighing);
    }
}
