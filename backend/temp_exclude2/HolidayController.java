package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.HolidayEntity;
import kr.co.softice.mes.domain.service.HolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Holiday Controller
 * 휴일 관리 컨트롤러
 *
 * @author Moon Myung-seop
 */
@Tag(name = "Holiday", description = "휴일 관리 API")
@Slf4j
@RestController
@RequestMapping("/api/holidays")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    /**
     * Get all holidays by tenant
     */
    @Operation(summary = "휴일 목록 조회", description = "테넌트의 모든 휴일을 조회합니다")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<HolidayEntity>>> getAllHolidays(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            log.debug("GET /api/holidays - tenantId: {}", tenantId);
            List<HolidayEntity> holidays = holidayService.findAllHolidays(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(true)
                            .data(holidays)
                            .message("휴일 목록 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get all holidays", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(false)
                            .message("휴일 목록 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get active holidays by tenant
     */
    @Operation(summary = "활성 휴일 조회", description = "테넌트의 활성화된 휴일을 조회합니다")
    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<HolidayEntity>>> getActiveHolidays(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            log.debug("GET /api/holidays/active - tenantId: {}", tenantId);
            List<HolidayEntity> holidays = holidayService.findActiveHolidays(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(true)
                            .data(holidays)
                            .message("활성 휴일 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get active holidays", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(false)
                            .message("활성 휴일 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get holidays by year
     */
    @Operation(summary = "연도별 휴일 조회", description = "특정 연도의 휴일을 조회합니다")
    @GetMapping("/year/{year}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<HolidayEntity>>> getHolidaysByYear(
            @Parameter(description = "연도", required = true, example = "2026")
            @PathVariable int year,
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            log.debug("GET /api/holidays/year/{} - tenantId: {}", year, tenantId);
            List<HolidayEntity> holidays = holidayService.findHolidaysByYear(tenantId, year);
            return ResponseEntity.ok(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(true)
                            .data(holidays)
                            .message("연도별 휴일 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get holidays by year", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(false)
                            .message("연도별 휴일 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get holidays by date range
     */
    @Operation(summary = "기간별 휴일 조회", description = "특정 기간의 휴일을 조회합니다")
    @GetMapping("/range")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<HolidayEntity>>> getHolidaysByDateRange(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "시작일", required = true, example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일", required = true, example = "2026-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.debug("GET /api/holidays/range - tenantId: {}, startDate: {}, endDate: {}",
                    tenantId, startDate, endDate);
            List<HolidayEntity> holidays = holidayService.findHolidaysByDateRange(tenantId, startDate, endDate);
            return ResponseEntity.ok(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(true)
                            .data(holidays)
                            .message("기간별 휴일 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get holidays by date range", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(false)
                            .message("기간별 휴일 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get holiday by ID
     */
    @Operation(summary = "휴일 상세 조회", description = "ID로 휴일을 조회합니다")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<HolidayEntity>> getHolidayById(
            @Parameter(description = "휴일 ID", required = true)
            @PathVariable Long id) {
        try {
            log.debug("GET /api/holidays/{}", id);
            Optional<HolidayEntity> holiday = holidayService.findHolidayById(id);
            if (holiday.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.<HolidayEntity>builder()
                                .success(true)
                                .data(holiday.get())
                                .message("휴일 조회 성공")
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.<HolidayEntity>builder()
                                .success(false)
                                .message("휴일을 찾을 수 없습니다")
                                .errorCode(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Failed to get holiday by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<HolidayEntity>builder()
                            .success(false)
                            .message("휴일 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get holidays by type
     */
    @Operation(summary = "타입별 휴일 조회", description = "휴일 타입(NATIONAL, COMPANY, SPECIAL)별로 조회합니다")
    @GetMapping("/type/{holidayType}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<HolidayEntity>>> getHolidaysByType(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "휴일 타입", required = true, example = "NATIONAL")
            @PathVariable String holidayType) {
        try {
            log.debug("GET /api/holidays/type/{} - tenantId: {}", holidayType, tenantId);
            List<HolidayEntity> holidays = holidayService.findHolidaysByType(tenantId, holidayType);
            return ResponseEntity.ok(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(true)
                            .data(holidays)
                            .message("타입별 휴일 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get holidays by type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(false)
                            .message("타입별 휴일 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get national holidays
     */
    @Operation(summary = "국경일 조회", description = "국경일(NATIONAL)만 조회합니다")
    @GetMapping("/national")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<HolidayEntity>>> getNationalHolidays(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            log.debug("GET /api/holidays/national - tenantId: {}", tenantId);
            List<HolidayEntity> holidays = holidayService.findNationalHolidays(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(true)
                            .data(holidays)
                            .message("국경일 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get national holidays", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<HolidayEntity>>builder()
                            .success(false)
                            .message("국경일 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Create holiday
     */
    @Operation(summary = "휴일 생성", description = "새로운 휴일을 생성합니다")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<HolidayEntity>> createHoliday(
            @Parameter(description = "휴일 정보", required = true)
            @Valid @RequestBody HolidayEntity holiday) {
        try {
            log.info("POST /api/holidays - holidayName: {}, holidayDate: {}",
                    holiday.getHolidayName(), holiday.getHolidayDate());
            HolidayEntity created = holidayService.createHoliday(holiday);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<HolidayEntity>builder()
                            .success(true)
                            .data(created)
                            .message("휴일 생성 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create holiday", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HolidayEntity>builder()
                            .success(false)
                            .message("휴일 생성 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Update holiday
     */
    @Operation(summary = "휴일 수정", description = "휴일 정보를 수정합니다")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<HolidayEntity>> updateHoliday(
            @Parameter(description = "휴일 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "휴일 정보", required = true)
            @Valid @RequestBody HolidayEntity holiday) {
        try {
            log.info("PUT /api/holidays/{} - holidayName: {}", id, holiday.getHolidayName());
            holiday.setHolidayId(id);
            HolidayEntity updated = holidayService.updateHoliday(holiday);
            return ResponseEntity.ok(
                    ApiResponse.<HolidayEntity>builder()
                            .success(true)
                            .data(updated)
                            .message("휴일 수정 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to update holiday", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<HolidayEntity>builder()
                            .success(false)
                            .message("휴일 수정 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Delete holiday
     */
    @Operation(summary = "휴일 삭제", description = "휴일을 삭제합니다")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteHoliday(
            @Parameter(description = "휴일 ID", required = true)
            @PathVariable Long id) {
        try {
            log.info("DELETE /api/holidays/{}", id);
            holidayService.deleteHoliday(id);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("휴일 삭제 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to delete holiday", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("휴일 삭제 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    // ==================== Business Day Calculation APIs ====================

    /**
     * Check if date is a business day
     */
    @Operation(summary = "영업일 확인", description = "특정 날짜가 영업일인지 확인합니다")
    @GetMapping("/business-day/check")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkBusinessDay(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "확인할 날짜", required = true, example = "2026-01-25")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.debug("GET /api/holidays/business-day/check - tenantId: {}, date: {}", tenantId, date);
            boolean isBusinessDay = holidayService.isBusinessDay(tenantId, date);

            Map<String, Object> result = new HashMap<>();
            result.put("date", date);
            result.put("isBusinessDay", isBusinessDay);
            result.put("dayOfWeek", date.getDayOfWeek().toString());

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .data(result)
                            .message("영업일 확인 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to check business day", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("영업일 확인 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Calculate business days between two dates
     */
    @Operation(summary = "영업일 계산", description = "두 날짜 사이의 영업일 수를 계산합니다")
    @GetMapping("/business-day/calculate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculateBusinessDays(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "시작일", required = true, example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일", required = true, example = "2026-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.debug("GET /api/holidays/business-day/calculate - tenantId: {}, startDate: {}, endDate: {}",
                    tenantId, startDate, endDate);
            long businessDays = holidayService.calculateBusinessDays(tenantId, startDate, endDate);

            Map<String, Object> result = new HashMap<>();
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("businessDays", businessDays);
            result.put("totalDays", java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .data(result)
                            .message("영업일 계산 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to calculate business days", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("영업일 계산 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Add business days to a date
     */
    @Operation(summary = "영업일 더하기", description = "특정 날짜에 영업일을 더한 날짜를 계산합니다")
    @GetMapping("/business-day/add")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addBusinessDays(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "시작일", required = true, example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "더할 영업일 수", required = true, example = "5")
            @RequestParam int businessDaysToAdd) {
        try {
            log.debug("GET /api/holidays/business-day/add - tenantId: {}, startDate: {}, businessDaysToAdd: {}",
                    tenantId, startDate, businessDaysToAdd);
            LocalDate resultDate = holidayService.addBusinessDays(tenantId, startDate, businessDaysToAdd);

            Map<String, Object> result = new HashMap<>();
            result.put("startDate", startDate);
            result.put("businessDaysAdded", businessDaysToAdd);
            result.put("resultDate", resultDate);
            result.put("dayOfWeek", resultDate.getDayOfWeek().toString());

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .data(result)
                            .message("영업일 더하기 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to add business days", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("영업일 더하기 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get next business day
     */
    @Operation(summary = "다음 영업일 조회", description = "특정 날짜 이후의 다음 영업일을 조회합니다")
    @GetMapping("/business-day/next")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNextBusinessDay(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "기준 날짜", required = true, example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.debug("GET /api/holidays/business-day/next - tenantId: {}, date: {}", tenantId, date);
            LocalDate nextBusinessDay = holidayService.getNextBusinessDay(tenantId, date);

            Map<String, Object> result = new HashMap<>();
            result.put("referenceDate", date);
            result.put("nextBusinessDay", nextBusinessDay);
            result.put("dayOfWeek", nextBusinessDay.getDayOfWeek().toString());

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .data(result)
                            .message("다음 영업일 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get next business day", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("다음 영업일 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get previous business day
     */
    @Operation(summary = "이전 영업일 조회", description = "특정 날짜 이전의 영업일을 조회합니다")
    @GetMapping("/business-day/previous")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPreviousBusinessDay(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "기준 날짜", required = true, example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.debug("GET /api/holidays/business-day/previous - tenantId: {}, date: {}", tenantId, date);
            LocalDate previousBusinessDay = holidayService.getPreviousBusinessDay(tenantId, date);

            Map<String, Object> result = new HashMap<>();
            result.put("referenceDate", date);
            result.put("previousBusinessDay", previousBusinessDay);
            result.put("dayOfWeek", previousBusinessDay.getDayOfWeek().toString());

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .data(result)
                            .message("이전 영업일 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get previous business day", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("이전 영업일 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Count holidays in date range
     */
    @Operation(summary = "기간 내 휴일 수 조회", description = "특정 기간 내의 휴일 수를 조회합니다")
    @GetMapping("/count")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> countHolidaysInRange(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "시작일", required = true, example = "2026-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일", required = true, example = "2026-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.debug("GET /api/holidays/count - tenantId: {}, startDate: {}, endDate: {}",
                    tenantId, startDate, endDate);
            long holidayCount = holidayService.countHolidaysInRange(tenantId, startDate, endDate);

            Map<String, Object> result = new HashMap<>();
            result.put("startDate", startDate);
            result.put("endDate", endDate);
            result.put("holidayCount", holidayCount);
            result.put("totalDays", java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1);

            return ResponseEntity.ok(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(true)
                            .data(result)
                            .message("휴일 수 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to count holidays", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<Map<String, Object>>builder()
                            .success(false)
                            .message("휴일 수 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }
}
