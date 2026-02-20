package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.HolidayEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Holiday Controller
 * 휴일 관리 REST API
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
@Tag(name = "Holiday", description = "휴일 관리 API")
public class HolidayController {

    private final HolidayService holidayService;
    private final TenantRepository tenantRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "휴일 목록 조회", description = "모든 휴일을 조회합니다.")
    public ResponseEntity<List<HolidayEntity>> getAllHolidays() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all holidays for tenant: {}", tenantId);
        List<HolidayEntity> holidays = holidayService.findAllHolidays(tenantId);
        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 휴일 목록 조회", description = "활성 상태의 휴일을 조회합니다.")
    public ResponseEntity<List<HolidayEntity>> getActiveHolidays() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active holidays for tenant: {}", tenantId);
        List<HolidayEntity> holidays = holidayService.findActiveHolidays(tenantId);
        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/{holidayId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "휴일 상세 조회", description = "ID로 휴일을 조회합니다.")
    public ResponseEntity<HolidayEntity> getHolidayById(@PathVariable Long holidayId) {
        log.info("Getting holiday by ID: {}", holidayId);
        HolidayEntity holiday = holidayService.findHolidayById(holidayId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.HOLIDAY_NOT_FOUND));
        return ResponseEntity.ok(holiday);
    }

    @GetMapping("/year/{year}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "연도별 휴일 조회", description = "특정 연도의 휴일을 조회합니다.")
    public ResponseEntity<List<HolidayEntity>> getHolidaysByYear(@PathVariable int year) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting holidays for year: {} in tenant: {}", year, tenantId);
        List<HolidayEntity> holidays = holidayService.findHolidaysByYear(tenantId, year);
        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/range")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "기간별 휴일 조회", description = "기간 내 휴일을 조회합니다.")
    public ResponseEntity<List<HolidayEntity>> getHolidaysByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting holidays from {} to {} for tenant: {}", startDate, endDate, tenantId);
        List<HolidayEntity> holidays = holidayService.findHolidaysByDateRange(tenantId, startDate, endDate);
        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/type/{holidayType}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "유형별 휴일 조회", description = "특정 유형의 휴일을 조회합니다.")
    public ResponseEntity<List<HolidayEntity>> getHolidaysByType(@PathVariable String holidayType) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting holidays by type: {} for tenant: {}", holidayType, tenantId);
        List<HolidayEntity> holidays = holidayService.findHolidaysByType(tenantId, holidayType);
        return ResponseEntity.ok(holidays);
    }

    @GetMapping("/check-business-day")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "영업일 확인", description = "특정 날짜가 영업일인지 확인합니다.")
    public ResponseEntity<Boolean> isBusinessDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        String tenantId = TenantContext.getCurrentTenant();
        boolean result = holidayService.isBusinessDay(tenantId, date);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/business-days")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "영업일 수 계산", description = "기간 내 영업일 수를 계산합니다.")
    public ResponseEntity<Long> calculateBusinessDays(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        long days = holidayService.calculateBusinessDays(tenantId, startDate, endDate);
        return ResponseEntity.ok(days);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Operation(summary = "휴일 등록", description = "새로운 휴일을 등록합니다.")
    public ResponseEntity<HolidayEntity> createHoliday(@RequestBody HolidayEntity holiday) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating holiday: {} on {} for tenant: {}", holiday.getHolidayName(), holiday.getHolidayDate(), tenantId);

        TenantEntity tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND));
        holiday.setTenant(tenant);

        HolidayEntity created = holidayService.createHoliday(holiday);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{holidayId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM_MANAGER')")
    @Operation(summary = "휴일 수정", description = "휴일 정보를 수정합니다.")
    public ResponseEntity<HolidayEntity> updateHoliday(
            @PathVariable Long holidayId,
            @RequestBody HolidayEntity holiday) {
        log.info("Updating holiday ID: {}", holidayId);
        holiday.setHolidayId(holidayId);
        HolidayEntity updated = holidayService.updateHoliday(holiday);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{holidayId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "휴일 삭제", description = "휴일을 삭제합니다.")
    public ResponseEntity<Void> deleteHoliday(@PathVariable Long holidayId) {
        log.info("Deleting holiday ID: {}", holidayId);
        holidayService.deleteHoliday(holidayId);
        return ResponseEntity.ok().build();
    }
}
