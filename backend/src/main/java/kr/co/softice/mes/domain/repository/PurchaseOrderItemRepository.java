package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.PurchaseOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Purchase Order Item Repository
 * 구매 주문 상세 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItemEntity, Long> {

    List<PurchaseOrderItemEntity> findByPurchaseOrder_PurchaseOrderId(Long purchaseOrderId);
}
