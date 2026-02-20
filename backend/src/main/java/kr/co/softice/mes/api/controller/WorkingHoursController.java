package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.WorkingHoursEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.WorkingHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Working Hours Controller
 * 근무 시간 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/working-hours")
@RequiredArgsConstructor
@Tag(name = "WorkingHours", description = "근무 시간 관리 API")
public class WorkingHoursController {

    private final WorkingHoursRepository workingHoursRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "근무 시간 목록 조회", description = "모든 근무 시간 설정을 조회합니다.")
    public ResponseEntity<List<WorkingHoursEntity>> getAllWorkingHours() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all working hours for tenant: {}", tenantId);
        List<WorkingHoursEntity> schedules = workingHoursRepository.findAllByTenantId(tenantId);
        return ResponseEntity.ok(schedules);
    }

    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 근무 시간 조회", description = "활성 상태의 근무 시간 설정을 조회합니다.")
    public ResponseEntity<List<WorkingHoursEntity>> getActiveWorkingHours() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active working hours for tenant: {}", tenantId);
        List<WorkingHoursEntity> schedules = workingHoursRepository.findActiveByTenantId(tenantId);
        return ResponseEntity.ok(schedules);
    }

    @Transactional(readOnly = true)
    @GetMapping("/{workingHoursId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "근무 시간 상세 조회", description = "ID로 근무 시간 설정을 조회합니다.")
    public ResponseEntity<WorkingHoursEntity> getWorkingHoursById(@PathVariable Long workingHoursId) {
        log.info("Getting working hours by ID: {}", workingHoursId);
        WorkingHoursEntity schedule = workingHoursRepository.findById(workingHoursId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));
        return ResponseEntity.ok(schedule);
    }

    @Transactional(readOnly = true)
    @GetMapping("/default")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기본 근무 시간 조회", description = "기본 근무 시간 설정을 조회합니다.")
    public ResponseEntity<WorkingHoursEntity> getDefaultWorkingHours() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting default working hours for tenant: {}", tenantId);
        WorkingHoursEntity schedule = workingHoursRepository.findDefaultByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND,
                        "기본 근무 시간 설정을 찾을 수 없습니다."));
        return ResponseEntity.ok(schedule);
    }

    @Transactional(readOnly = true)
    @GetMapping("/effective")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "유효 근무 시간 조회", description = "특정 날짜에 유효한 근무 시간을 조회합니다.")
    public ResponseEntity<List<WorkingHoursEntity>> getEffectiveWorkingHours(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting effective working hours for date: {} in tenant: {}", date, tenantId);
        List<WorkingHoursEntity> schedules = workingHoursRepository.findEffectiveByTenantIdAndDate(tenantId, date);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Transactional
    @Operation(summary = "근무 시간 등록", description = "새로운 근무 시간 설정을 등록합니다.")
    public ResponseEntity<WorkingHoursEntity> createWorkingHours(@RequestBody WorkingHoursEntity schedule) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating working hours: {} for tenant: {}", schedule.getScheduleName(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));

        if (workingHoursRepository.existsByTenantIdAndScheduleName(tenantId, schedule.getScheduleName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "근무 시간 설정이 이미 존재합니다: " + schedule.getScheduleName());
        }

        schedule.setTenant(tenant);
        WorkingHoursEntity saved = workingHoursRepository.save(schedule);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{workingHoursId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Transactional
    @Operation(summary = "근무 시간 수정", description = "근무 시간 설정을 수정합니다.")
    public ResponseEntity<WorkingHoursEntity> updateWorkingHours(
            @PathVariable Long workingHoursId,
            @RequestBody WorkingHoursEntity schedule) {
        log.info("Updating working hours ID: {}", workingHoursId);

        WorkingHoursEntity existing = workingHoursRepository.findById(workingHoursId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        existing.setScheduleName(schedule.getScheduleName());
        existing.setDescription(schedule.getDescription());
        existing.setMondayStart(schedule.getMondayStart());
        existing.setMondayEnd(schedule.getMondayEnd());
        existing.setTuesdayStart(schedule.getTuesdayStart());
        existing.setTuesdayEnd(schedule.getTuesdayEnd());
        existing.setWednesdayStart(schedule.getWednesdayStart());
        existing.setWednesdayEnd(schedule.getWednesdayEnd());
        existing.setThursdayStart(schedule.getThursdayStart());
        existing.setThursdayEnd(schedule.getThursdayEnd());
        existing.setFridayStart(schedule.getFridayStart());
        existing.setFridayEnd(schedule.getFridayEnd());
        existing.setSaturdayStart(schedule.getSaturdayStart());
        existing.setSaturdayEnd(schedule.getSaturdayEnd());
        existing.setSundayStart(schedule.getSundayStart());
        existing.setSundayEnd(schedule.getSundayEnd());
        existing.setBreakStart1(schedule.getBreakStart1());
        existing.setBreakEnd1(schedule.getBreakEnd1());
        existing.setBreakStart2(schedule.getBreakStart2());
        existing.setBreakEnd2(schedule.getBreakEnd2());
        existing.setEffectiveFrom(schedule.getEffectiveFrom());
        existing.setEffectiveTo(schedule.getEffectiveTo());
        existing.setIsDefault(schedule.getIsDefault());
        existing.setIsActive(schedule.getIsActive());

        WorkingHoursEntity updated = workingHoursRepository.save(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{workingHoursId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Operation(summary = "근무 시간 삭제", description = "근무 시간 설정을 삭제합니다.")
    public ResponseEntity<Void> deleteWorkingHours(@PathVariable Long workingHoursId) {
        log.info("Deleting working hours ID: {}", workingHoursId);

        WorkingHoursEntity schedule = workingHoursRepository.findById(workingHoursId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RESOURCE_NOT_FOUND));

        workingHoursRepository.delete(schedule);
        return ResponseEntity.ok().build();
    }
}
