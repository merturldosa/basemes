package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.HolidayEntity;
import kr.co.softice.mes.domain.entity.WorkingHoursEntity;
import kr.co.softice.mes.domain.repository.HolidayRepository;
import kr.co.softice.mes.domain.repository.WorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Holiday Service
 * 휴일 관리 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayService {

    private final HolidayRepository holidayRepository;
    private final WorkingHoursRepository workingHoursRepository;

    /**
     * Find all holidays
     */
    @Transactional(readOnly = true)
    public List<HolidayEntity> findAllHolidays(String tenantId) {
        log.debug("Finding all holidays for tenant: {}", tenantId);
        return holidayRepository.findAllByTenantId(tenantId);
    }

    /**
     * Find active holidays
     */
    @Transactional(readOnly = true)
    public List<HolidayEntity> findActiveHolidays(String tenantId) {
        log.debug("Finding active holidays for tenant: {}", tenantId);
        return holidayRepository.findActiveByTenantId(tenantId);
    }

    /**
     * Find holidays by year
     */
    @Transactional(readOnly = true)
    public List<HolidayEntity> findHolidaysByYear(String tenantId, int year) {
        log.debug("Finding holidays for year: {} in tenant: {}", year, tenantId);
        return holidayRepository.findByTenantIdAndYear(tenantId, year);
    }

    /**
     * Find holidays by date range
     */
    @Transactional(readOnly = true)
    public List<HolidayEntity> findHolidaysByDateRange(String tenantId, LocalDate startDate, LocalDate endDate) {
        log.debug("Finding holidays from {} to {} for tenant: {}", startDate, endDate, tenantId);
        return holidayRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
    }

    /**
     * Find holiday by ID
     */
    @Transactional(readOnly = true)
    public Optional<HolidayEntity> findHolidayById(Long holidayId) {
        return holidayRepository.findById(holidayId);
    }

    /**
     * Find holidays by type
     */
    @Transactional(readOnly = true)
    public List<HolidayEntity> findHolidaysByType(String tenantId, String holidayType) {
        return holidayRepository.findByTenantIdAndHolidayType(tenantId, holidayType);
    }

    /**
     * Find national holidays
     */
    @Transactional(readOnly = true)
    public List<HolidayEntity> findNationalHolidays(String tenantId) {
        return holidayRepository.findNationalHolidaysByTenantId(tenantId);
    }

    /**
     * Create holiday
     */
    @Transactional
    public HolidayEntity createHoliday(HolidayEntity holiday) {
        log.info("Creating holiday: {} on {} for tenant: {}",
                holiday.getHolidayName(), holiday.getHolidayDate(), holiday.getTenant().getTenantId());

        // Check duplicate
        String tenantId = holiday.getTenant().getTenantId();
        if (holidayRepository.existsByTenantIdAndDate(tenantId, holiday.getHolidayDate())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Holiday already exists on date: " + holiday.getHolidayDate());
        }

        return holidayRepository.save(holiday);
    }

    /**
     * Update holiday
     */
    @Transactional
    public HolidayEntity updateHoliday(HolidayEntity holiday) {
        log.info("Updating holiday: {}", holiday.getHolidayId());

        HolidayEntity existing = holidayRepository.findById(holiday.getHolidayId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Update fields
        existing.setHolidayName(holiday.getHolidayName());
        existing.setHolidayDate(holiday.getHolidayDate());
        existing.setHolidayType(holiday.getHolidayType());
        existing.setIsRecurring(holiday.getIsRecurring());
        existing.setRecurrenceRule(holiday.getRecurrenceRule());
        existing.setIsWorkingDay(holiday.getIsWorkingDay());
        existing.setDescription(holiday.getDescription());
        existing.setRemarks(holiday.getRemarks());
        existing.setIsActive(holiday.getIsActive());

        return holidayRepository.save(existing);
    }

    /**
     * Delete holiday
     */
    @Transactional
    public void deleteHoliday(Long holidayId) {
        log.info("Deleting holiday: {}", holidayId);

        HolidayEntity holiday = holidayRepository.findById(holidayId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        holidayRepository.delete(holiday);
    }

    // ==================== Business Day Calculation ====================

    /**
     * Check if date is a business day
     */
    public boolean isBusinessDay(String tenantId, LocalDate date) {
        // Check if it's a holiday
        if (holidayRepository.isHoliday(tenantId, date)) {
            return false;
        }

        // Check if it's a weekend (Saturday or Sunday)
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            // Check working hours to see if weekends are working days
            Optional<WorkingHoursEntity> workingHours = workingHoursRepository.findDefaultByTenantId(tenantId);
            if (workingHours.isPresent()) {
                int dow = dayOfWeek.getValue();
                return workingHours.get().isWorkingDay(dow);
            }
            return false; // Default: weekends are not working days
        }

        return true;
    }

    /**
     * Calculate business days between two dates
     */
    public long calculateBusinessDays(String tenantId, LocalDate startDate, LocalDate endDate) {
        long businessDays = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isBusinessDay(tenantId, currentDate)) {
                businessDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return businessDays;
    }

    /**
     * Add business days to a date
     */
    public LocalDate addBusinessDays(String tenantId, LocalDate startDate, int businessDaysToAdd) {
        LocalDate resultDate = startDate;
        int addedDays = 0;

        while (addedDays < businessDaysToAdd) {
            resultDate = resultDate.plusDays(1);
            if (isBusinessDay(tenantId, resultDate)) {
                addedDays++;
            }
        }

        return resultDate;
    }

    /**
     * Get next business day
     */
    public LocalDate getNextBusinessDay(String tenantId, LocalDate date) {
        LocalDate nextDay = date.plusDays(1);
        while (!isBusinessDay(tenantId, nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    /**
     * Get previous business day
     */
    public LocalDate getPreviousBusinessDay(String tenantId, LocalDate date) {
        LocalDate previousDay = date.minusDays(1);
        while (!isBusinessDay(tenantId, previousDay)) {
            previousDay = previousDay.minusDays(1);
        }
        return previousDay;
    }

    /**
     * Count holidays in date range
     */
    public long countHolidaysInRange(String tenantId, LocalDate startDate, LocalDate endDate) {
        return holidayRepository.countHolidaysInRange(tenantId, startDate, endDate);
    }
}
