package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.SalesOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Sales Order Item Repository
 * 판매 주문 상세 리포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItemEntity, Long> {
}
