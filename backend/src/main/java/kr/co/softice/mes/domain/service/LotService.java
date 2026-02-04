package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            throw new IllegalArgumentException("Lot number already exists: " + lot.getLotNo());
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
    public LotEntity updateQualityStatus(Long lotId, String qualityStatus) {
        LotEntity lot = lotRepository.findById(lotId)
            .orElseThrow(() -> new IllegalArgumentException("Lot not found: " + lotId));

        log.info("Updating lot {} quality status from {} to {}",
            lot.getLotNo(), lot.getQualityStatus(), qualityStatus);

        lot.setQualityStatus(qualityStatus);
        LotEntity updated = lotRepository.save(lot);
        return lotRepository.findByIdWithAllRelations(updated.getLotId()).orElse(updated);
    }
}
