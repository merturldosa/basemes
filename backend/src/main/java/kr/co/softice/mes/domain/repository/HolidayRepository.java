package kr.co.softice.mes.domain.repository;

import kr.co.softice.mes.domain.entity.HolidayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Holiday Repository
 * 휴일 레포지토리
 *
 * @author Moon Myung-seop
 */
@Repository
public interface HolidayRepository extends JpaRepository<HolidayEntity, Long> {

    /**
     * Find all holidays by tenant ID
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "ORDER BY h.holidayDate ASC")
    List<HolidayEntity> findAllByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find active holidays by tenant ID
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.isActive = true " +
            "ORDER BY h.holidayDate ASC")
    List<HolidayEntity> findActiveByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find holidays by year
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND YEAR(h.holidayDate) = :year " +
            "AND h.isActive = true " +
            "ORDER BY h.holidayDate ASC")
    List<HolidayEntity> findByTenantIdAndYear(
            @Param("tenantId") String tenantId,
            @Param("year") int year);

    /**
     * Find holidays by date range
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.holidayDate BETWEEN :startDate AND :endDate " +
            "AND h.isActive = true " +
            "ORDER BY h.holidayDate ASC")
    List<HolidayEntity> findByTenantIdAndDateRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find holiday by exact date
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.holidayDate = :date " +
            "AND h.isActive = true")
    Optional<HolidayEntity> findByTenantIdAndDate(
            @Param("tenantId") String tenantId,
            @Param("date") LocalDate date);

    /**
     * Find holidays by type
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.holidayType = :holidayType " +
            "AND h.isActive = true " +
            "ORDER BY h.holidayDate ASC")
    List<HolidayEntity> findByTenantIdAndHolidayType(
            @Param("tenantId") String tenantId,
            @Param("holidayType") String holidayType);

    /**
     * Find national holidays
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.holidayType = 'NATIONAL' " +
            "AND h.isActive = true " +
            "ORDER BY h.holidayDate ASC")
    List<HolidayEntity> findNationalHolidaysByTenantId(@Param("tenantId") String tenantId);

    /**
     * Find non-working day holidays
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.isWorkingDay = false " +
            "AND h.isActive = true " +
            "ORDER BY h.holidayDate ASC")
    List<HolidayEntity> findNonWorkingDaysByTenantId(@Param("tenantId") String tenantId);

    /**
     * Check if date is a holiday
     */
    @Query("SELECT COUNT(h) > 0 FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.holidayDate = :date " +
            "AND h.isWorkingDay = false " +
            "AND h.isActive = true")
    boolean isHoliday(
            @Param("tenantId") String tenantId,
            @Param("date") LocalDate date);

    /**
     * Count holidays in date range
     */
    @Query("SELECT COUNT(h) FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.holidayDate BETWEEN :startDate AND :endDate " +
            "AND h.isWorkingDay = false " +
            "AND h.isActive = true")
    Long countHolidaysInRange(
            @Param("tenantId") String tenantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Check if holiday exists for date
     */
    @Query("SELECT COUNT(h) > 0 FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.holidayDate = :date")
    boolean existsByTenantIdAndDate(
            @Param("tenantId") String tenantId,
            @Param("date") LocalDate date);

    /**
     * Find recurring holidays
     */
    @Query("SELECT h FROM HolidayEntity h " +
            "WHERE h.tenant.tenantId = :tenantId " +
            "AND h.isRecurring = true " +
            "AND h.isActive = true " +
            "ORDER BY h.holidayDate ASC")
    List<HolidayEntity> findRecurringHolidaysByTenantId(@Param("tenantId") String tenantId);
}
