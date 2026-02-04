package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.weighing.*;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.WeighingEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.WeighingRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.ProductRepository;
import kr.co.softice.mes.domain.repository.LotRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing weighing operations in compliance with GMP standards.
 * Handles weighing creation, verification, and tolerance checking.
 *
 * @author SoftIce MES Development Team
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WeighingService {

    private final WeighingRepository weighingRepository;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final LotRepository lotRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new weighing record.
     * Generates weighing number in format WG-YYYYMMDD-0001.
     *
     * @param tenantId tenant identifier
     * @param request weighing creation request
     * @return created weighing response
     * @throws BusinessException if tenant, product, lot, or operator not found
     */
    public WeighingResponse createWeighing(String tenantId, WeighingCreateRequest request) {
        log.info("Creating weighing for tenant: {}, product: {}", tenantId, request.getProductId());

        // Resolve tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND,
                        "Tenant not found: " + tenantId));

        // Resolve product
        ProductEntity product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found: " + request.getProductId()));

        // Resolve lot (optional)
        LotEntity lot = null;
        if (request.getLotId() != null) {
            lot = lotRepository.findById(request.getLotId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND,
                            "Lot not found: " + request.getLotId()));
        }

        // Resolve operator
        UserEntity operator = userRepository.findById(request.getOperatorUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "Operator not found: " + request.getOperatorUserId()));

        // Generate weighing number
        String weighingNo = generateWeighingNo(tenantId);

        // Create WeighingEntity
        WeighingEntity weighing = WeighingEntity.builder()
                .weighingNo(weighingNo)
                .tenant(tenant)
                .product(product)
                .lot(lot)
                .weighingType(request.getWeighingType())
                .weighingDate(request.getWeighingDate() != null ? request.getWeighingDate() : LocalDateTime.now())
                .grossWeight(request.getGrossWeight())
                .tareWeight(request.getTareWeight())
                .expectedWeight(request.getExpectedWeight())
                .tolerancePercentage(request.getTolerancePercentage())
                .operator(operator)
                .scaleId(request.getScaleId())
                .scaleName(request.getScaleName())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .unit(request.getUnit())
                .temperature(request.getTemperature())
                .humidity(request.getHumidity())
                .verificationStatus("PENDING")
                .remarks(request.getRemarks())
                .build();

        // Perform calculations
        weighing.performCalculations();

        // Save weighing
        WeighingEntity saved = weighingRepository.save(weighing);
        log.info("Created weighing: {} with status: {}", saved.getWeighingNo(), saved.getVerificationStatus());

        return convertToResponse(saved);
    }

    /**
     * Updates an existing weighing record.
     * Cannot update verified weighings.
     *
     * @param tenantId tenant identifier
     * @param weighingId weighing ID
     * @param request update request
     * @return updated weighing response
     * @throws BusinessException if weighing not found, tenant mismatch, or already verified
     */
    public WeighingResponse updateWeighing(String tenantId, Long weighingId, WeighingUpdateRequest request) {
        log.info("Updating weighing: {} for tenant: {}", weighingId, tenantId);

        // Get weighing with relations
        WeighingEntity weighing = weighingRepository.findByIdWithAllRelations(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEIGHING_NOT_FOUND,
                        "Weighing not found: " + weighingId));

        // Check tenant access
        if (!weighing.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "Weighing does not belong to tenant: " + tenantId);
        }

        // Cannot update if VERIFIED
        if ("VERIFIED".equals(weighing.getVerificationStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Cannot update verified weighing: " + weighing.getWeighingNo());
        }

        // Update non-null fields from request
        if (request.getWeighingDate() != null) {
            weighing.setWeighingDate(request.getWeighingDate());
        }
        if (request.getGrossWeight() != null) {
            weighing.setGrossWeight(request.getGrossWeight());
        }
        if (request.getTareWeight() != null) {
            weighing.setTareWeight(request.getTareWeight());
        }
        if (request.getExpectedWeight() != null) {
            weighing.setExpectedWeight(request.getExpectedWeight());
        }
        if (request.getTolerancePercentage() != null) {
            weighing.setTolerancePercentage(request.getTolerancePercentage());
        }
        if (request.getScaleId() != null) {
            weighing.setScaleId(request.getScaleId());
        }
        if (request.getScaleName() != null) {
            weighing.setScaleName(request.getScaleName());
        }
        if (request.getTemperature() != null) {
            weighing.setTemperature(request.getTemperature());
        }
        if (request.getHumidity() != null) {
            weighing.setHumidity(request.getHumidity());
        }
        if (request.getRemarks() != null) {
            weighing.setRemarks(request.getRemarks());
        }

        // Recalculate
        weighing.performCalculations();

        // Save
        WeighingEntity updated = weighingRepository.save(weighing);
        log.info("Updated weighing: {}, tolerance exceeded: {}",
                updated.getWeighingNo(), updated.getToleranceExceeded());

        return convertToResponse(updated);
    }

    /**
     * Verifies or rejects a weighing record.
     * Enforces GMP compliance by preventing self-verification.
     *
     * @param tenantId tenant identifier
     * @param weighingId weighing ID
     * @param request verification request
     * @return verified weighing response
     * @throws BusinessException if weighing not found, tenant mismatch, status invalid, or self-verification attempted
     */
    public WeighingResponse verifyWeighing(String tenantId, Long weighingId, WeighingVerificationRequest request) {
        log.info("Verifying weighing: {} for tenant: {}, action: {}",
                weighingId, tenantId, request.getAction());

        // Get weighing
        WeighingEntity weighing = weighingRepository.findByIdWithAllRelations(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEIGHING_NOT_FOUND,
                        "Weighing not found: " + weighingId));

        // Check tenant
        if (!weighing.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "Weighing does not belong to tenant: " + tenantId);
        }

        // Check PENDING status
        if (!"PENDING".equals(weighing.getVerificationStatus())) {
            throw new BusinessException(ErrorCode.INVALID_STATUS_TRANSITION,
                    "Weighing is not pending verification: " + weighing.getVerificationStatus());
        }

        // Resolve verifier
        UserEntity verifier = userRepository.findById(request.getVerifierUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND,
                        "Verifier not found: " + request.getVerifierUserId()));

        // Check not self-verification (GMP compliance)
        if (weighing.getOperator() != null &&
            weighing.getOperator().getUserId().equals(verifier.getUserId())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Self-verification is not allowed (GMP compliance)");
        }

        // Verify or reject based on action
        if ("VERIFY".equalsIgnoreCase(request.getAction())) {
            weighing.verify(verifier, request.getRemarks());
            log.info("Weighing verified: {} by user: {}", weighing.getWeighingNo(), verifier.getUserId());
        } else if ("REJECT".equalsIgnoreCase(request.getAction())) {
            weighing.reject(verifier, request.getRemarks());
            log.info("Weighing rejected: {} by user: {}", weighing.getWeighingNo(), verifier.getUserId());
        } else {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "Invalid verification action: " + request.getAction());
        }

        // Save
        WeighingEntity verified = weighingRepository.save(weighing);

        return convertToResponse(verified);
    }

    /**
     * Retrieves a weighing record by ID.
     *
     * @param tenantId tenant identifier
     * @param weighingId weighing ID
     * @return weighing response
     * @throws BusinessException if weighing not found or tenant mismatch
     */
    @Transactional(readOnly = true)
    public WeighingResponse getWeighingById(String tenantId, Long weighingId) {
        log.debug("Getting weighing: {} for tenant: {}", weighingId, tenantId);

        WeighingEntity weighing = weighingRepository.findByIdWithAllRelations(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEIGHING_NOT_FOUND,
                        "Weighing not found: " + weighingId));

        if (!weighing.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "Weighing does not belong to tenant: " + tenantId);
        }

        return convertToResponse(weighing);
    }

    /**
     * Retrieves all weighing records for a tenant.
     *
     * @param tenantId tenant identifier
     * @return list of weighing responses
     */
    @Transactional(readOnly = true)
    public List<WeighingResponse> getAllWeighings(String tenantId) {
        log.debug("Getting all weighings for tenant: {}", tenantId);

        List<WeighingEntity> weighings = weighingRepository.findByTenantIdWithAllRelations(tenantId);

        return weighings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves weighings by reference type and ID.
     *
     * @param tenantId tenant identifier
     * @param referenceType reference type (e.g., WORK_ORDER, PURCHASE_ORDER)
     * @param referenceId reference ID
     * @return list of weighing responses
     */
    @Transactional(readOnly = true)
    public List<WeighingResponse> getWeighingsByReference(String tenantId, String referenceType, Long referenceId) {
        log.debug("Getting weighings for tenant: {}, reference: {}/{}", tenantId, referenceType, referenceId);

        List<WeighingEntity> weighings = weighingRepository
                .findByReferenceTypeAndReferenceIdWithRelations(referenceType, referenceId);

        // Filter by tenant ID in service layer
        return weighings.stream()
                .filter(w -> w.getTenant().getTenantId().equals(tenantId))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves weighings that exceed tolerance limits.
     *
     * @param tenantId tenant identifier
     * @return list of weighing responses with tolerance exceeded
     */
    @Transactional(readOnly = true)
    public List<WeighingResponse> getToleranceExceededWeighings(String tenantId) {
        log.debug("Getting tolerance exceeded weighings for tenant: {}", tenantId);

        List<WeighingEntity> weighings = weighingRepository
                .findToleranceExceededWeighings(tenantId);

        return weighings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves weighings pending verification.
     *
     * @param tenantId tenant identifier
     * @return list of weighing responses pending verification
     */
    @Transactional(readOnly = true)
    public List<WeighingResponse> getPendingVerificationWeighings(String tenantId) {
        log.debug("Getting pending verification weighings for tenant: {}", tenantId);

        List<WeighingEntity> weighings = weighingRepository
                .findPendingVerificationWeighings(tenantId);

        return weighings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Deletes a weighing record.
     * Cannot delete verified weighings (GMP compliance).
     *
     * @param tenantId tenant identifier
     * @param weighingId weighing ID
     * @throws BusinessException if weighing not found, tenant mismatch, or weighing is verified
     */
    public void deleteWeighing(String tenantId, Long weighingId) {
        log.info("Deleting weighing: {} for tenant: {}", weighingId, tenantId);

        WeighingEntity weighing = weighingRepository.findById(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEIGHING_NOT_FOUND,
                        "Weighing not found: " + weighingId));

        if (!weighing.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED,
                    "Weighing does not belong to tenant: " + tenantId);
        }

        // Cannot delete VERIFIED weighings (GMP)
        if ("VERIFIED".equals(weighing.getVerificationStatus())) {
            throw new BusinessException(ErrorCode.INVALID_OPERATION,
                    "Cannot delete verified weighing (GMP compliance): " + weighing.getWeighingNo());
        }

        weighingRepository.delete(weighing);
        log.info("Deleted weighing: {}", weighing.getWeighingNo());
    }

    /**
     * Finds weighings by tenant (for controller compatibility).
     *
     * @param tenantId tenant identifier
     * @return list of weighing entities
     */
    public List<WeighingEntity> findByTenant(String tenantId) {
        log.debug("Finding weighings by tenant: {}", tenantId);
        return weighingRepository.findByTenant_TenantId(tenantId);
    }

    /**
     * Finds weighings by type (for controller compatibility).
     *
     * @param tenantId tenant identifier
     * @param weighingType weighing type
     * @return list of weighing entities
     */
    public List<WeighingEntity> findByType(String tenantId, String weighingType) {
        log.debug("Finding weighings by tenant: {}, type: {}", tenantId, weighingType);
        return weighingRepository.findByTenant_TenantIdAndWeighingType(tenantId, weighingType);
    }

    /**
     * Finds weighings by verification status (for controller compatibility).
     *
     * @param tenantId tenant identifier
     * @param status verification status
     * @return list of weighing entities
     */
    public List<WeighingEntity> findByVerificationStatus(String tenantId, String status) {
        log.debug("Finding weighings by tenant: {}, status: {}", tenantId, status);
        return weighingRepository.findByTenant_TenantIdAndVerificationStatus(tenantId, status);
    }

    /**
     * Generates weighing number in format WG-YYYYMMDD-0001.
     *
     * @param tenantId tenant identifier
     * @return generated weighing number
     */
    private String generateWeighingNo(String tenantId) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "WG-" + dateStr + "-";

        // Find last weighing number for today by filtering in Java code
        List<WeighingEntity> allWeighings = weighingRepository.findByTenant_TenantId(tenantId);

        List<WeighingEntity> todayWeighings = allWeighings.stream()
                .filter(w -> w.getWeighingNo() != null && w.getWeighingNo().startsWith(prefix))
                .sorted((w1, w2) -> w2.getWeighingNo().compareTo(w1.getWeighingNo()))
                .collect(Collectors.toList());

        int nextSeq = 1;
        if (!todayWeighings.isEmpty()) {
            String lastNo = todayWeighings.get(0).getWeighingNo();
            String seqStr = lastNo.substring(lastNo.lastIndexOf('-') + 1);
            try {
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (NumberFormatException e) {
                log.warn("Failed to parse sequence from weighing number: {}", lastNo);
                nextSeq = 1;
            }
        }

        String weighingNo = prefix + String.format("%04d", nextSeq);
        log.debug("Generated weighing number: {}", weighingNo);

        return weighingNo;
    }

    /**
     * Converts WeighingEntity to WeighingResponse DTO.
     *
     * @param weighing weighing entity
     * @return weighing response DTO
     */
    private WeighingResponse convertToResponse(WeighingEntity weighing) {
        return WeighingResponse.builder()
                .weighingId(weighing.getWeighingId())
                .weighingNo(weighing.getWeighingNo())
                .tenantId(weighing.getTenant().getTenantId())
                .weighingDate(weighing.getWeighingDate())
                .weighingType(weighing.getWeighingType())
                .referenceType(weighing.getReferenceType())
                .referenceId(weighing.getReferenceId())
                .productId(weighing.getProduct().getProductId())
                .productCode(weighing.getProduct().getProductCode())
                .productName(weighing.getProduct().getProductName())
                .lotId(weighing.getLot() != null ? weighing.getLot().getLotId() : null)
                .lotNo(weighing.getLot() != null ? weighing.getLot().getLotNo() : null)
                .tareWeight(weighing.getTareWeight())
                .grossWeight(weighing.getGrossWeight())
                .netWeight(weighing.getNetWeight())
                .expectedWeight(weighing.getExpectedWeight())
                .variance(weighing.getVariance())
                .variancePercentage(weighing.getVariancePercentage())
                .unit(weighing.getUnit())
                .scaleId(weighing.getScaleId())
                .scaleName(weighing.getScaleName())
                .operatorUserId(weighing.getOperator() != null ? weighing.getOperator().getUserId() : null)
                .operatorUsername(weighing.getOperator() != null ? weighing.getOperator().getUsername() : null)
                .operatorName(weighing.getOperator() != null ? weighing.getOperator().getFullName() : null)
                .verifierUserId(weighing.getVerifier() != null ? weighing.getVerifier().getUserId() : null)
                .verifierUsername(weighing.getVerifier() != null ? weighing.getVerifier().getUsername() : null)
                .verifierName(weighing.getVerifier() != null ? weighing.getVerifier().getFullName() : null)
                .verificationDate(weighing.getVerificationDate())
                .verificationStatus(weighing.getVerificationStatus())
                .toleranceExceeded(weighing.getToleranceExceeded())
                .tolerancePercentage(weighing.getTolerancePercentage())
                .remarks(weighing.getRemarks())
                .temperature(weighing.getTemperature())
                .humidity(weighing.getHumidity())
                .createdAt(weighing.getCreatedAt())
                .updatedAt(weighing.getUpdatedAt())
                .build();
    }
}
