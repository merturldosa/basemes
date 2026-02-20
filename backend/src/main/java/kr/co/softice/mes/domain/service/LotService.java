package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Lot Service
 * LOT/배치 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotService {

    private final LotRepository lotRepository;

    public List<LotEntity> findByTenant(String tenantId) {
        return lotRepository.findByTenantIdWithAllRelations(tenantId);
    }

    public List<LotEntity> findByTenantAndProduct(String tenantId, Long productId) {
        return lotRepository.findByTenant_TenantIdAndProduct_ProductId(tenantId, productId);
    }

    public List<LotEntity> findByTenantAndQualityStatus(String tenantId, String qualityStatus) {
        return lotRepository.findByTenant_TenantIdAndQualityStatus(tenantId, qualityStatus);
    }

    public Optional<LotEntity> findById(Long lotId) {
        return lotRepository.findByIdWithAllRelations(lotId);
    }

    public Optional<LotEntity> findByLotNo(String tenantId, String lotNo) {
        return lotRepository.findByTenant_TenantIdAndLotNo(tenantId, lotNo);
    }

    @Transactional
    public LotEntity createLot(LotEntity lot) {
        log.info("Creating lot: {} for product: {} with quality status: {}",
            lot.getLotNo(), lot.getProduct().getProductCode(), lot.getQualityStatus());

        if (lotRepository.existsByTenantAndLotNo(lot.getTenant(), lot.getLotNo())) {
            throw new BusinessException(ErrorCode.LOT_ALREADY_EXISTS);
        }

        LotEntity saved = lotRepository.save(lot);
        return lotRepository.findByIdWithAllRelations(saved.getLotId()).orElse(saved);
    }

    @Transactional
    public LotEntity updateLot(LotEntity lot) {
        log.info("Updating lot: {}", lot.getLotId());
        LotEntity updated = lotRepository.save(lot);
        return lotRepository.findByIdWithAllRelations(updated.getLotId()).orElse(updated);
    }

    @Transactional
    public void deleteLot(Long lotId) {
        log.info("Deleting lot: {}", lotId);
        lotRepository.deleteById(lotId);
    }

    @Transactional
    public LotEntity splitLot(Long parentLotId, BigDecimal splitQuantity, String remarks) {
        LotEntity parentLot = lotRepository.findByIdWithAllRelations(parentLotId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND));

        // Validate split quantity
        if (splitQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (splitQuantity.compareTo(parentLot.getCurrentQuantity()) >= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // Generate child LOT number (lotNo-S01, lotNo-S02, ...)
        String baseLotNo = parentLot.getLotNo();
        int splitIndex = 1;
        String childLotNo;
        do {
            childLotNo = baseLotNo + "-S" + String.format("%02d", splitIndex);
            splitIndex++;
        } while (lotRepository.findByTenant_TenantIdAndLotNo(
                parentLot.getTenant().getTenantId(), childLotNo).isPresent());

        // Reduce parent LOT quantity
        BigDecimal newParentQty = parentLot.getCurrentQuantity().subtract(splitQuantity);
        parentLot.setCurrentQuantity(newParentQty);
        lotRepository.save(parentLot);

        log.info("Split LOT {}: parent qty {} -> {}, child LOT {} qty {}",
            parentLot.getLotNo(), parentLot.getCurrentQuantity().add(splitQuantity),
            newParentQty, childLotNo, splitQuantity);

        // Create child LOT
        LotEntity childLot = LotEntity.builder()
            .tenant(parentLot.getTenant())
            .product(parentLot.getProduct())
            .lotNo(childLotNo)
            .batchNo(parentLot.getBatchNo())
            .manufacturingDate(parentLot.getManufacturingDate())
            .expiryDate(parentLot.getExpiryDate())
            .initialQuantity(splitQuantity)
            .currentQuantity(splitQuantity)
            .reservedQuantity(BigDecimal.ZERO)
            .unit(parentLot.getUnit())
            .supplierName(parentLot.getSupplierName())
            .supplierLotNo(parentLot.getSupplierLotNo())
            .qualityStatus(parentLot.getQualityStatus())
            .workOrder(parentLot.getWorkOrder())
            .remarks(remarks != null ? remarks : "Split from " + parentLot.getLotNo())
            .build();

        LotEntity savedChild = lotRepository.save(childLot);
        return lotRepository.findByIdWithAllRelations(savedChild.getLotId()).orElse(savedChild);
    }

    @Transactional
    public LotEntity updateQualityStatus(Long lotId, String qualityStatus) {
        LotEntity lot = lotRepository.findById(lotId)
            .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND));

        log.info("Updating lot {} quality status from {} to {}",
            lot.getLotNo(), lot.getQualityStatus(), qualityStatus);

        lot.setQualityStatus(qualityStatus);
        LotEntity updated = lotRepository.save(lot);
        return lotRepository.findByIdWithAllRelations(updated.getLotId()).orElse(updated);
    }
}
