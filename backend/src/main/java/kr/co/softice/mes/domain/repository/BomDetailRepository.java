package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.BomDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BOM Detail Repository
 * BOM 상세 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface BomDetailRepository extends JpaRepository<BomDetailEntity, Long> {

    List<BomDetailEntity> findByBom_BomId(Long bomId);
    List<BomDetailEntity> findByBom_BomIdOrderBySequenceAsc(Long bomId);
    void deleteByBom_BomId(Long bomId);
}
