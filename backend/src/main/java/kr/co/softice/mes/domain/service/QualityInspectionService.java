package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Quality Inspection Service
 * 품질 검사 기록 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QualityInspectionService {

    private final QualityInspectionRepository qualityInspectionRepository;
    private final QualityStandardRepository qualityStandardRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * Find all quality inspections by tenant ID
     */
    public List<QualityInspectionEntity> findByTenant(String tenantId) {
        return qualityInspectionRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Find quality inspection by ID
     */
    public Optional<QualityInspectionEntity> findById(Long id) {
        return qualityInspectionRepository.findByIdWithAllRelations(id);
    }

    /**
     * Find quality inspections by work order ID
     */
    public List<QualityInspectionEntity> findByWorkOrderId(Long workOrderId) {
        return qualityInspectionRepository.findByWorkOrderIdWithRelations(workOrderId);
    }

    /**
     * Find quality inspections by inspection result
     */
    public List<QualityInspectionEntity> findByResult(String tenantId, String result) {
        return qualityInspectionRepository.findByTenantIdAndResultWithRelations(tenantId, result);
    }

    /**
     * Find quality inspections by inspection type
     */
    public List<QualityInspectionEntity> findByInspectionType(String tenantId, String type) {
        return qualityInspectionRepository.findByTenantIdAndTypeWithRelations(tenantId, type);
    }


    /**
     * Create new quality inspection
     * Automatically determines inspection result based on quality standard criteria
     * and calculates pass/fail quantities
     */
    @Transactional
    public QualityInspectionEntity createQualityInspection(QualityInspectionEntity inspection) {
        log.info("Creating quality inspection: {} for tenant: {}",
            inspection.getInspectionNo(), inspection.getTenant().getTenantId());

        // Check duplicate inspection number
        if (qualityInspectionRepository.existsByTenantAndInspectionNo(
                inspection.getTenant(), inspection.getInspectionNo())) {
            throw new BusinessException(ErrorCode.QUALITY_INSPECTION_ALREADY_EXISTS);
        }

        // Determine inspection result based on measured value and quality standard
        QualityStandardEntity standard = inspection.getQualityStandard();
        if (inspection.getMeasuredValue() != null && standard != null) {
            String result = determineInspectionResult(inspection.getMeasuredValue(), standard);
            inspection.setInspectionResult(result);
        }

        // Calculate pass/fail quantities based on inspection result
        calculatePassFailQuantities(inspection);

        return qualityInspectionRepository.save(inspection);
    }

    /**
     * Update quality inspection
     * Re-determines inspection result if measured value is changed
     */
    @Transactional
    public QualityInspectionEntity updateQualityInspection(QualityInspectionEntity inspection) {
        log.info("Updating quality inspection: {}", inspection.getQualityInspectionId());

        if (!qualityInspectionRepository.existsById(inspection.getQualityInspectionId())) {
            throw new BusinessException(ErrorCode.QUALITY_INSPECTION_NOT_FOUND);
        }

        // Re-determine inspection result based on measured value and quality standard
        QualityStandardEntity standard = inspection.getQualityStandard();
        if (inspection.getMeasuredValue() != null && standard != null) {
            String result = determineInspectionResult(inspection.getMeasuredValue(), standard);
            inspection.setInspectionResult(result);
        }

        // Recalculate pass/fail quantities based on inspection result
        calculatePassFailQuantities(inspection);

        return qualityInspectionRepository.save(inspection);
    }

    /**
     * Delete quality inspection
     */
    @Transactional
    public void deleteQualityInspection(Long id) {
        log.info("Deleting quality inspection: {}", id);
        qualityInspectionRepository.deleteById(id);
    }

    /**
     * Count quality inspections by tenant and result
     */
    public long countByTenantAndResult(String tenantId, String result) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        return qualityInspectionRepository.countByTenantAndInspectionResult(tenant, result);
    }

    /**
     * Count quality inspections by tenant
     */
    public long countByTenant(String tenantId) {
        TenantEntity tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        return qualityInspectionRepository.countByTenant(tenant);
    }

    /**
     * Calculate pass rate for tenant
     * Returns percentage of passed inspections
     */
    public double calculatePassRate(String tenantId) {
        long totalCount = countByTenant(tenantId);
        if (totalCount == 0) {
            return 0.0;
        }

        long passedCount = countByTenantAndResult(tenantId, "PASS");
        return (double) passedCount / totalCount * 100.0;
    }

    /**
     * Determine inspection result based on measured value and quality standard
     *
     * Logic:
     * - PASS: measured value is within min/max range
     * - FAIL: measured value is outside min/max range
     * - CONDITIONAL: measured value is within tolerance but outside optimal range
     *
     * @param measuredValue The measured value from inspection
     * @param standard Quality standard containing min/max/tolerance criteria
     * @return Inspection result: "PASS", "FAIL", or "CONDITIONAL"
     */
    private String determineInspectionResult(BigDecimal measuredValue, QualityStandardEntity standard) {
        // If no criteria defined, default to PASS
        if (standard.getMinValue() == null && standard.getMaxValue() == null) {
            return "PASS";
        }

        // Check against min/max values
        boolean withinMinMax = true;

        if (standard.getMinValue() != null && measuredValue.compareTo(standard.getMinValue()) < 0) {
            withinMinMax = false;
        }

        if (standard.getMaxValue() != null && measuredValue.compareTo(standard.getMaxValue()) > 0) {
            withinMinMax = false;
        }

        // If outside min/max range, it's a FAIL
        if (!withinMinMax) {
            // Check if within tolerance range (CONDITIONAL instead of FAIL)
            if (standard.getToleranceValue() != null) {
                BigDecimal lowerTolerance = standard.getMinValue() != null
                    ? standard.getMinValue().subtract(standard.getToleranceValue())
                    : null;
                BigDecimal upperTolerance = standard.getMaxValue() != null
                    ? standard.getMaxValue().add(standard.getToleranceValue())
                    : null;

                boolean withinTolerance = true;
                if (lowerTolerance != null && measuredValue.compareTo(lowerTolerance) < 0) {
                    withinTolerance = false;
                }
                if (upperTolerance != null && measuredValue.compareTo(upperTolerance) > 0) {
                    withinTolerance = false;
                }

                return withinTolerance ? "CONDITIONAL" : "FAIL";
            }
            return "FAIL";
        }

        return "PASS";
    }

    /**
     * Calculate pass/fail quantities based on inspection result
     *
     * Logic:
     * - PASS: passedQuantity = inspectedQuantity, failedQuantity = 0
     * - FAIL: passedQuantity = 0, failedQuantity = inspectedQuantity
     * - CONDITIONAL: passedQuantity = inspectedQuantity, failedQuantity = 0
     *
     * @param inspection Quality inspection entity to update
     */
    private void calculatePassFailQuantities(QualityInspectionEntity inspection) {
        BigDecimal inspectedQty = inspection.getInspectedQuantity();
        if (inspectedQty == null) {
            inspectedQty = BigDecimal.ZERO;
        }

        String result = inspection.getInspectionResult();
        if ("FAIL".equals(result)) {
            inspection.setPassedQuantity(BigDecimal.ZERO);
            inspection.setFailedQuantity(inspectedQty);
        } else {
            // For PASS and CONDITIONAL, treat as passed
            inspection.setPassedQuantity(inspectedQty);
            inspection.setFailedQuantity(BigDecimal.ZERO);
        }

        log.debug("Calculated quantities for inspection {}: result={}, passed={}, failed={}",
            inspection.getInspectionNo(), result,
            inspection.getPassedQuantity(), inspection.getFailedQuantity());
    }

    /**
     * Find inspections requiring retest
     *
     * Returns failed inspections that have a corrective action defined
     * but not yet completed (corrective_action_date is null)
     *
     * @param tenantId Tenant ID
     * @return List of inspections requiring retest
     */
    public List<QualityInspectionEntity> findRetestRequired(String tenantId) {
        log.info("Finding retest required inspections for tenant: {}", tenantId);

        List<QualityInspectionEntity> failedInspections =
            qualityInspectionRepository.findByTenantIdAndResultWithRelations(tenantId, "FAIL");

        // Filter for inspections with corrective action defined but not yet completed
        return failedInspections.stream()
            .filter(qi -> qi.getCorrectiveAction() != null &&
                         !qi.getCorrectiveAction().isEmpty() &&
                         qi.getCorrectiveActionDate() == null)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Find failed items for returns processing
     *
     * Returns all failed inspections that can be used to create
     * return documents for rejected materials/products
     *
     * @param tenantId Tenant ID
     * @return List of failed inspections
     */
    public List<QualityInspectionEntity> findFailedItemsForReturns(String tenantId) {
        log.info("Finding failed items for returns for tenant: {}", tenantId);

        return qualityInspectionRepository.findByTenantIdAndResultWithRelations(tenantId, "FAIL");
    }
}
