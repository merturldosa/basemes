package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.CodeEntity;
import kr.co.softice.mes.domain.entity.CodeGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Code Repository
 *
 * @author Moon Myung-seop
 */
@Repository
public interface CodeRepository extends JpaRepository<CodeEntity, Long> {

    /**
     * Find by code group and code
     */
    Optional<CodeEntity> findByCodeGroupAndCode(CodeGroupEntity codeGroup, String code);

    /**
     * Find by code group
     */
    List<CodeEntity> findByCodeGroup(CodeGroupEntity codeGroup);

    /**
     * Find by code group and status
     */
    List<CodeEntity> findByCodeGroupAndStatus(CodeGroupEntity codeGroup, String status);

    /**
     * Find by code group ordered by display order
     */
    List<CodeEntity> findByCodeGroupOrderByDisplayOrderAsc(CodeGroupEntity codeGroup);

    /**
     * Find by code group and status ordered by display order
     */
    List<CodeEntity> findByCodeGroupAndStatusOrderByDisplayOrderAsc(CodeGroupEntity codeGroup, String status);

    /**
     * Check if code exists for code group
     */
    boolean existsByCodeGroupAndCode(CodeGroupEntity codeGroup, String code);
}
