package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.EmployeeSkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Employee Skill Repository
 * 사원 스킬 리포지토리
 * @author Moon Myung-seop
 */
@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkillEntity, Long> {

    /**
     * Find all employee skills by tenant with all relations
     */
    @Query("SELECT es FROM EmployeeSkillEntity es " +
           "JOIN FETCH es.tenant " +
           "JOIN FETCH es.employee e " +
           "JOIN FETCH es.skill s " +
           "WHERE es.tenant.tenantId = :tenantId " +
           "ORDER BY e.employeeNo, s.skillCode")
    List<EmployeeSkillEntity> findByTenantIdWithAllRelations(@Param("tenantId") String tenantId);

    /**
     * Find employee skill by ID with all relations
     */
    @Query("SELECT es FROM EmployeeSkillEntity es " +
           "JOIN FETCH es.tenant " +
           "JOIN FETCH es.employee e " +
           "LEFT JOIN FETCH e.site " +
           "LEFT JOIN FETCH e.department " +
           "JOIN FETCH es.skill s " +
           "WHERE es.employeeSkillId = :employeeSkillId")
    Optional<EmployeeSkillEntity> findByIdWithAllRelations(@Param("employeeSkillId") Long employeeSkillId);

    /**
     * Find skills by employee
     */
    @Query("SELECT es FROM EmployeeSkillEntity es " +
           "JOIN FETCH es.tenant " +
           "JOIN FETCH es.employee e " +
           "JOIN FETCH es.skill s " +
           "WHERE es.employee.employeeId = :employeeId " +
           "AND es.isActive = true " +
           "ORDER BY s.skillCode")
    List<EmployeeSkillEntity> findByEmployeeId(@Param("employeeId") Long employeeId);

    /**
     * Find employees by skill
     */
    @Query("SELECT es FROM EmployeeSkillEntity es " +
           "JOIN FETCH es.tenant " +
           "JOIN FETCH es.employee e " +
           "LEFT JOIN FETCH e.site " +
           "LEFT JOIN FETCH e.department " +
           "JOIN FETCH es.skill s " +
           "WHERE es.tenant.tenantId = :tenantId " +
           "AND es.skill.skillId = :skillId " +
           "AND es.isActive = true " +
           "ORDER BY e.employeeNo")
    List<EmployeeSkillEntity> findByTenantIdAndSkillId(@Param("tenantId") String tenantId,
                                                         @Param("skillId") Long skillId);

    /**
     * Find employees by skill and minimum level
     */
    @Query("SELECT es FROM EmployeeSkillEntity es " +
           "JOIN FETCH es.tenant " +
           "JOIN FETCH es.employee e " +
           "LEFT JOIN FETCH e.site " +
           "LEFT JOIN FETCH e.department " +
           "JOIN FETCH es.skill s " +
           "WHERE es.tenant.tenantId = :tenantId " +
           "AND es.skill.skillId = :skillId " +
           "AND es.skillLevelNumeric >= :minLevel " +
           "AND es.isActive = true " +
           "ORDER BY es.skillLevelNumeric DESC, e.employeeNo")
    List<EmployeeSkillEntity> findByTenantIdAndSkillIdAndMinLevel(@Param("tenantId") String tenantId,
                                                                    @Param("skillId") Long skillId,
                                                                    @Param("minLevel") Integer minLevel);

    /**
     * Find expiring certifications
     */
    @Query("SELECT es FROM EmployeeSkillEntity es " +
           "JOIN FETCH es.tenant " +
           "JOIN FETCH es.employee e " +
           "JOIN FETCH es.skill s " +
           "WHERE es.tenant.tenantId = :tenantId " +
           "AND es.expiryDate IS NOT NULL " +
           "AND es.expiryDate <= :expiryDate " +
           "AND es.isActive = true " +
           "ORDER BY es.expiryDate, e.employeeNo")
    List<EmployeeSkillEntity> findExpiringCertifications(@Param("tenantId") String tenantId,
                                                          @Param("expiryDate") LocalDate expiryDate);

    /**
     * Find skills pending assessment
     */
    @Query("SELECT es FROM EmployeeSkillEntity es " +
           "JOIN FETCH es.tenant " +
           "JOIN FETCH es.employee e " +
           "JOIN FETCH es.skill s " +
           "WHERE es.tenant.tenantId = :tenantId " +
           "AND es.nextAssessmentDate IS NOT NULL " +
           "AND es.nextAssessmentDate <= :assessmentDate " +
           "AND es.isActive = true " +
           "ORDER BY es.nextAssessmentDate, e.employeeNo")
    List<EmployeeSkillEntity> findPendingAssessments(@Param("tenantId") String tenantId,
                                                      @Param("assessmentDate") LocalDate assessmentDate);

    /**
     * Check if employee has skill
     */
    boolean existsByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(String tenantId,
                                                                            Long employeeId,
                                                                            Long skillId);

    /**
     * Find by employee and skill
     */
    Optional<EmployeeSkillEntity> findByTenant_TenantIdAndEmployee_EmployeeIdAndSkill_SkillId(String tenantId,
                                                                                                 Long employeeId,
                                                                                                 Long skillId);

    /**
     * Count skills by employee
     */
    @Query("SELECT COUNT(es) FROM EmployeeSkillEntity es " +
           "WHERE es.employee.employeeId = :employeeId " +
           "AND es.isActive = true")
    Long countByEmployeeId(@Param("employeeId") Long employeeId);
}
