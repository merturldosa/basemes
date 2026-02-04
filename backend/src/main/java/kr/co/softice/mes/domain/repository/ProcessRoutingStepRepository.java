package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.ProcessRoutingStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Process Routing Step Repository
 * 공정 라우팅 단계 저장소
 *
 * @author Moon Myung-seop
 */
@Repository
public interface ProcessRoutingStepRepository extends JpaRepository<ProcessRoutingStepEntity, Long> {
}
