package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.WorkingHoursEntity;
import kr.co.softice.mes.domain.repository.WorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Working Hours Service
 * 근무 시간 설정 서비스
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;

    /**
     * Find all working hours by tenant
     */
    @Transactional(readOnly = true)
    public List<WorkingHoursEntity> findAllByTenantId(String tenantId) {
        log.debug("Finding all working hours for tenant: {}", tenantId);
        return workingHoursRepository.findAllByTenantId(tenantId);
    }

    /**
     * Find active working hours by tenant
     */
    @Transactional(readOnly = true)
    public List<WorkingHoursEntity> findActiveByTenantId(String tenantId) {
        log.debug("Finding active working hours for tenant: {}", tenantId);
        return workingHoursRepository.findActiveByTenantId(tenantId);
    }

    /**
     * Find default working hours
     */
    @Transactional(readOnly = true)
    public Optional<WorkingHoursEntity> findDefaultByTenantId(String tenantId) {
        log.debug("Finding default working hours for tenant: {}", tenantId);
        return workingHoursRepository.findDefaultByTenantId(tenantId);
    }

    /**
     * Find working hours effective on given date
     */
    @Transactional(readOnly = true)
    public List<WorkingHoursEntity> findEffectiveByTenantIdAndDate(String tenantId, LocalDate date) {
        log.debug("Finding effective working hours for tenant: {} on date: {}", tenantId, date);
        return workingHoursRepository.findEffectiveByTenantIdAndDate(tenantId, date);
    }

    /**
     * Find working hours by ID
     */
    @Transactional(readOnly = true)
    public Optional<WorkingHoursEntity> findById(Long workingHoursId) {
        return workingHoursRepository.findById(workingHoursId);
    }

    /**
     * Find working hours by schedule name
     */
    @Transactional(readOnly = true)
    public Optional<WorkingHoursEntity> findByTenantIdAndScheduleName(String tenantId, String scheduleName) {
        log.debug("Finding working hours by schedule name: {} for tenant: {}", scheduleName, tenantId);
        return workingHoursRepository.findByTenantIdAndScheduleName(tenantId, scheduleName);
    }

    /**
     * Create working hours
     */
    @Transactional
    public WorkingHoursEntity createWorkingHours(WorkingHoursEntity workingHours) {
        log.info("Creating working hours: {} for tenant: {}",
                workingHours.getScheduleName(), workingHours.getTenant().getTenantId());

        // Check duplicate schedule name
        String tenantId = workingHours.getTenant().getTenantId();
        if (workingHoursRepository.existsByTenantIdAndScheduleName(tenantId, workingHours.getScheduleName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Working hours with schedule name already exists: " + workingHours.getScheduleName());
        }

        // If this is set as default, remove default flag from other schedules
        if (workingHours.getIsDefault() != null && workingHours.getIsDefault()) {
            removeDefaultFlags(tenantId);
        }

        return workingHoursRepository.save(workingHours);
    }

    /**
     * Update working hours
     */
    @Transactional
    public WorkingHoursEntity updateWorkingHours(WorkingHoursEntity workingHours) {
        log.info("Updating working hours: {}", workingHours.getWorkingHoursId());

        WorkingHoursEntity existing = workingHoursRepository.findById(workingHours.getWorkingHoursId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // If this is being set as default, remove default flag from other schedules
        if (workingHours.getIsDefault() != null && workingHours.getIsDefault() &&
                (existing.getIsDefault() == null || !existing.getIsDefault())) {
            removeDefaultFlags(existing.getTenant().getTenantId());
        }

        // Update fields
        existing.setScheduleName(workingHours.getScheduleName());
        existing.setDescription(workingHours.getDescription());

        // Update day-of-week working hours
        existing.setMondayStart(workingHours.getMondayStart());
        existing.setMondayEnd(workingHours.getMondayEnd());
        existing.setTuesdayStart(workingHours.getTuesdayStart());
        existing.setTuesdayEnd(workingHours.getTuesdayEnd());
        existing.setWednesdayStart(workingHours.getWednesdayStart());
        existing.setWednesdayEnd(workingHours.getWednesdayEnd());
        existing.setThursdayStart(workingHours.getThursdayStart());
        existing.setThursdayEnd(workingHours.getThursdayEnd());
        existing.setFridayStart(workingHours.getFridayStart());
        existing.setFridayEnd(workingHours.getFridayEnd());
        existing.setSaturdayStart(workingHours.getSaturdayStart());
        existing.setSaturdayEnd(workingHours.getSaturdayEnd());
        existing.setSundayStart(workingHours.getSundayStart());
        existing.setSundayEnd(workingHours.getSundayEnd());

        // Update break times
        existing.setBreakStart1(workingHours.getBreakStart1());
        existing.setBreakEnd1(workingHours.getBreakEnd1());
        existing.setBreakStart2(workingHours.getBreakStart2());
        existing.setBreakEnd2(workingHours.getBreakEnd2());

        // Update effective period
        existing.setEffectiveFrom(workingHours.getEffectiveFrom());
        existing.setEffectiveTo(workingHours.getEffectiveTo());

        // Update flags
        existing.setIsDefault(workingHours.getIsDefault());
        existing.setIsActive(workingHours.getIsActive());

        return workingHoursRepository.save(existing);
    }

    /**
     * Delete working hours
     */
    @Transactional
    public void deleteWorkingHours(Long workingHoursId) {
        log.info("Deleting working hours: {}", workingHoursId);

        WorkingHoursEntity workingHours = workingHoursRepository.findById(workingHoursId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Check if it's the default schedule
        if (workingHours.getIsDefault() != null && workingHours.getIsDefault()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Cannot delete default working hours. Set another schedule as default first.");
        }

        workingHoursRepository.delete(workingHours);
    }

    /**
     * Set working hours as default
     */
    @Transactional
    public WorkingHoursEntity setAsDefault(Long workingHoursId, String tenantId) {
        log.info("Setting working hours {} as default for tenant: {}", workingHoursId, tenantId);

        WorkingHoursEntity workingHours = workingHoursRepository.findById(workingHoursId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        // Verify tenant match
        if (!workingHours.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Working hours does not belong to the specified tenant");
        }

        // Remove default flag from other schedules
        removeDefaultFlags(tenantId);

        // Set this as default
        workingHours.setIsDefault(true);
        return workingHoursRepository.save(workingHours);
    }

    /**
     * Remove default flag from all working hours in a tenant
     */
    private void removeDefaultFlags(String tenantId) {
        log.debug("Removing default flags from all working hours for tenant: {}", tenantId);
        List<WorkingHoursEntity> allWorkingHours = workingHoursRepository.findAllByTenantId(tenantId);
        for (WorkingHoursEntity wh : allWorkingHours) {
            if (wh.getIsDefault() != null && wh.getIsDefault()) {
                wh.setIsDefault(false);
                workingHoursRepository.save(wh);
            }
        }
    }

    /**
     * Check if schedule name exists
     */
    @Transactional(readOnly = true)
    public boolean existsByScheduleName(String tenantId, String scheduleName) {
        return workingHoursRepository.existsByTenantIdAndScheduleName(tenantId, scheduleName);
    }

    /**
     * Get working hours for specific day of week
     */
    @Transactional(readOnly = true)
    public WorkingHoursEntity.WorkingHours getWorkingHoursForDay(String tenantId, int dayOfWeek) {
        Optional<WorkingHoursEntity> workingHours = findDefaultByTenantId(tenantId);
        if (workingHours.isPresent()) {
            return workingHours.get().getWorkingHoursForDay(dayOfWeek);
        }
        return null;
    }

    /**
     * Check if given day of week is a working day
     */
    @Transactional(readOnly = true)
    public boolean isWorkingDay(String tenantId, int dayOfWeek) {
        Optional<WorkingHoursEntity> workingHours = findDefaultByTenantId(tenantId);
        if (workingHours.isPresent()) {
            return workingHours.get().isWorkingDay(dayOfWeek);
        }
        // Default: Monday-Friday are working days
        return dayOfWeek >= 1 && dayOfWeek <= 5;
    }
}
