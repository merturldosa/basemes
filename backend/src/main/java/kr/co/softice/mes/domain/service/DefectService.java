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

/**
 * Defect Service
 * 불량 관리 서비스
 * @author Moon Myung-seop
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DefectService {

    private final DefectRepository defectRepository;
    private final TenantRepository tenantRepository;
    private final ProductRepository productRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkResultRepository workResultRepository;
    private final GoodsReceiptRepository goodsReceiptRepository;
    private final ShippingRepository shippingRepository;
    private final QualityInspectionRepository qualityInspectionRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    /**
     * Get all defects for tenant
     */
    public List<DefectEntity> getAllDefects(String tenantId) {
        log.info("Getting all defects for tenant: {}", tenantId);
        return defectRepository.findByTenantIdWithAllRelations(tenantId);
    }

    /**
     * Get defect by ID
     */
    public DefectEntity getDefectById(Long defectId) {
        log.info("Getting defect by ID: {}", defectId);
        return defectRepository.findByIdWithAllRelations(defectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEFECT_NOT_FOUND));
    }

    /**
     * Get defects by status
     */
    public List<DefectEntity> getDefectsByStatus(String tenantId, String status) {
        log.info("Getting defects for tenant: {} with status: {}", tenantId, status);
        return defectRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * Get defects by source type
     */
    public List<DefectEntity> getDefectsBySourceType(String tenantId, String sourceType) {
        log.info("Getting defects for tenant: {} with source type: {}", tenantId, sourceType);
        return defectRepository.findByTenantIdAndSourceType(tenantId, sourceType);
    }

    /**
     * Create defect
     */
    @Transactional
    public DefectEntity createDefect(String tenantId, DefectEntity defect) {
        log.info("Creating defect: {} for tenant: {}", defect.getDefectNo(), tenantId);

        // Check duplicate
        if (defectRepository.existsByTenant_TenantIdAndDefectNo(tenantId, defect.getDefectNo())) {
            throw new BusinessException(ErrorCode.DEFECT_ALREADY_EXISTS);
        }

        // Get tenant
        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND));
        defect.setTenant(tenant);

        // Get product
        ProductEntity product = productRepository.findById(defect.getProduct().getProductId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        defect.setProduct(product);
        defect.setProductCode(product.getProductCode());
        defect.setProductName(product.getProductName());

        // Set optional relations
        if (defect.getWorkOrder() != null && defect.getWorkOrder().getWorkOrderId() != null) {
            WorkOrderEntity workOrder = workOrderRepository.findByIdWithAllRelations(defect.getWorkOrder().getWorkOrderId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_ORDER_NOT_FOUND));
            defect.setWorkOrder(workOrder);
        }

        if (defect.getWorkResult() != null && defect.getWorkResult().getWorkResultId() != null) {
            WorkResultEntity workResult = workResultRepository.findById(defect.getWorkResult().getWorkResultId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.WORK_RESULT_NOT_FOUND));
            defect.setWorkResult(workResult);
        }

        if (defect.getGoodsReceipt() != null && defect.getGoodsReceipt().getGoodsReceiptId() != null) {
            GoodsReceiptEntity goodsReceipt = goodsReceiptRepository.findByIdWithAllRelations(defect.getGoodsReceipt().getGoodsReceiptId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.GOODS_RECEIPT_NOT_FOUND));
            defect.setGoodsReceipt(goodsReceipt);
        }

        if (defect.getShipping() != null && defect.getShipping().getShippingId() != null) {
            ShippingEntity shipping = shippingRepository.findByIdWithAllRelations(defect.getShipping().getShippingId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.SHIPPING_NOT_FOUND));
            defect.setShipping(shipping);
        }

        if (defect.getQualityInspection() != null && defect.getQualityInspection().getQualityInspectionId() != null) {
            QualityInspectionEntity qualityInspection = qualityInspectionRepository.findByIdWithAllRelations(defect.getQualityInspection().getQualityInspectionId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.QUALITY_INSPECTION_NOT_FOUND));
            defect.setQualityInspection(qualityInspection);
        }

        if (defect.getResponsibleDepartment() != null && defect.getResponsibleDepartment().getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findByIdWithAllRelations(defect.getResponsibleDepartment().getDepartmentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND));
            defect.setResponsibleDepartment(department);
        }

        if (defect.getResponsibleUser() != null && defect.getResponsibleUser().getUserId() != null) {
            UserEntity responsibleUser = userRepository.findById(defect.getResponsibleUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            defect.setResponsibleUser(responsibleUser);
        }

        if (defect.getReporterUser() != null && defect.getReporterUser().getUserId() != null) {
            UserEntity reporterUser = userRepository.findById(defect.getReporterUser().getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
            defect.setReporterUser(reporterUser);
            defect.setReporterName(reporterUser.getFullName());
        }

        // Set defaults
        if (defect.getDefectQuantity() == null) {
            defect.setDefectQuantity(BigDecimal.ZERO);
        }
        if (defect.getDefectCost() == null) {
            defect.setDefectCost(BigDecimal.ZERO);
        }
        if (defect.getIsActive() == null) {
            defect.setIsActive(true);
        }
        if (defect.getStatus() == null) {
            defect.setStatus("REPORTED");
        }

        DefectEntity saved = defectRepository.save(defect);
        log.info("Defect created successfully: {}", saved.getDefectNo());
        return saved;
    }

    /**
     * Update defect
     */
    @Transactional
    public DefectEntity updateDefect(Long defectId, DefectEntity updateData) {
        log.info("Updating defect ID: {}", defectId);

        DefectEntity existing = defectRepository.findByIdWithAllRelations(defectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEFECT_NOT_FOUND));

        // Update fields
        if (updateData.getDefectType() != null) {
            existing.setDefectType(updateData.getDefectType());
        }
        if (updateData.getDefectCategory() != null) {
            existing.setDefectCategory(updateData.getDefectCategory());
        }
        if (updateData.getDefectLocation() != null) {
            existing.setDefectLocation(updateData.getDefectLocation());
        }
        if (updateData.getDefectDescription() != null) {
            existing.setDefectDescription(updateData.getDefectDescription());
        }
        if (updateData.getDefectQuantity() != null) {
            existing.setDefectQuantity(updateData.getDefectQuantity());
        }
        if (updateData.getSeverity() != null) {
            existing.setSeverity(updateData.getSeverity());
        }
        if (updateData.getStatus() != null) {
            existing.setStatus(updateData.getStatus());
        }
        if (updateData.getRootCause() != null) {
            existing.setRootCause(updateData.getRootCause());
        }
        if (updateData.getCorrectiveAction() != null) {
            existing.setCorrectiveAction(updateData.getCorrectiveAction());
        }
        if (updateData.getPreventiveAction() != null) {
            existing.setPreventiveAction(updateData.getPreventiveAction());
        }
        if (updateData.getDefectCost() != null) {
            existing.setDefectCost(updateData.getDefectCost());
        }
        if (updateData.getRemarks() != null) {
            existing.setRemarks(updateData.getRemarks());
        }

        // Update action date if corrective action is provided
        if (updateData.getCorrectiveAction() != null && existing.getActionDate() == null) {
            existing.setActionDate(LocalDateTime.now());
        }

        DefectEntity updated = defectRepository.save(existing);
        log.info("Defect updated successfully: {}", updated.getDefectNo());
        return updated;
    }

    /**
     * Close defect
     */
    @Transactional
    public DefectEntity closeDefect(Long defectId) {
        log.info("Closing defect ID: {}", defectId);

        DefectEntity defect = defectRepository.findByIdWithAllRelations(defectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEFECT_NOT_FOUND));

        defect.setStatus("CLOSED");
        if (defect.getActionDate() == null) {
            defect.setActionDate(LocalDateTime.now());
        }

        DefectEntity closed = defectRepository.save(defect);
        log.info("Defect closed successfully: {}", closed.getDefectNo());
        return closed;
    }

    /**
     * Delete defect
     */
    @Transactional
    public void deleteDefect(Long defectId) {
        log.info("Deleting defect ID: {}", defectId);

        DefectEntity defect = defectRepository.findById(defectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEFECT_NOT_FOUND));

        defectRepository.delete(defect);
        log.info("Defect deleted successfully: {}", defect.getDefectNo());
    }
}
