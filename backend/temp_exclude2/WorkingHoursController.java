package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.WorkingHoursEntity;
import kr.co.softice.mes.domain.service.WorkingHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Working Hours Controller
 * 근무 시간 설정 컨트롤러
 *
 * @author Moon Myung-seop
 */
@Tag(name = "Working Hours", description = "근무 시간 설정 API")
@Slf4j
@RestController
@RequestMapping("/api/working-hours")
@RequiredArgsConstructor
public class WorkingHoursController {

    private final WorkingHoursService workingHoursService;

    /**
     * Get all working hours by tenant
     */
    @Operation(summary = "근무 시간 목록 조회", description = "테넌트의 모든 근무 시간 설정을 조회합니다")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'HR_MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<WorkingHoursEntity>>> getAllWorkingHours(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            log.debug("GET /api/working-hours - tenantId: {}", tenantId);
            List<WorkingHoursEntity> workingHours = workingHoursService.findAllByTenantId(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<List<WorkingHoursEntity>>builder()
                            .success(true)
                            .data(workingHours)
                            .message("근무 시간 목록 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get all working hours", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<WorkingHoursEntity>>builder()
                            .success(false)
                            .message("근무 시간 목록 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get active working hours by tenant
     */
    @Operation(summary = "활성 근무 시간 조회", description = "테넌트의 활성화된 근무 시간 설정을 조회합니다")
    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'HR_MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<WorkingHoursEntity>>> getActiveWorkingHours(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            log.debug("GET /api/working-hours/active - tenantId: {}", tenantId);
            List<WorkingHoursEntity> workingHours = workingHoursService.findActiveByTenantId(tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<List<WorkingHoursEntity>>builder()
                            .success(true)
                            .data(workingHours)
                            .message("활성 근무 시간 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get active working hours", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<WorkingHoursEntity>>builder()
                            .success(false)
                            .message("활성 근무 시간 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get default working hours
     */
    @Operation(summary = "기본 근무 시간 조회", description = "테넌트의 기본 근무 시간 설정을 조회합니다")
    @GetMapping("/default")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'HR_MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<WorkingHoursEntity>> getDefaultWorkingHours(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            log.debug("GET /api/working-hours/default - tenantId: {}", tenantId);
            Optional<WorkingHoursEntity> workingHours = workingHoursService.findDefaultByTenantId(tenantId);
            if (workingHours.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.<WorkingHoursEntity>builder()
                                .success(true)
                                .data(workingHours.get())
                                .message("기본 근무 시간 조회 성공")
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.<WorkingHoursEntity>builder()
                                .success(false)
                                .message("기본 근무 시간을 찾을 수 없습니다")
                                .errorCode(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Failed to get default working hours", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(false)
                            .message("기본 근무 시간 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get working hours effective on given date
     */
    @Operation(summary = "유효 근무 시간 조회", description = "특정 날짜에 유효한 근무 시간 설정을 조회합니다")
    @GetMapping("/effective")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'HR_MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<List<WorkingHoursEntity>>> getEffectiveWorkingHours(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "기준 날짜", required = true, example = "2026-01-25")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.debug("GET /api/working-hours/effective - tenantId: {}, date: {}", tenantId, date);
            List<WorkingHoursEntity> workingHours = workingHoursService.findEffectiveByTenantIdAndDate(tenantId, date);
            return ResponseEntity.ok(
                    ApiResponse.<List<WorkingHoursEntity>>builder()
                            .success(true)
                            .data(workingHours)
                            .message("유효 근무 시간 조회 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get effective working hours", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<List<WorkingHoursEntity>>builder()
                            .success(false)
                            .message("유효 근무 시간 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get working hours by ID
     */
    @Operation(summary = "근무 시간 상세 조회", description = "ID로 근무 시간 설정을 조회합니다")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'HR_MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<WorkingHoursEntity>> getWorkingHoursById(
            @Parameter(description = "근무 시간 ID", required = true)
            @PathVariable Long id) {
        try {
            log.debug("GET /api/working-hours/{}", id);
            Optional<WorkingHoursEntity> workingHours = workingHoursService.findById(id);
            if (workingHours.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.<WorkingHoursEntity>builder()
                                .success(true)
                                .data(workingHours.get())
                                .message("근무 시간 조회 성공")
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.<WorkingHoursEntity>builder()
                                .success(false)
                                .message("근무 시간을 찾을 수 없습니다")
                                .errorCode(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Failed to get working hours by ID", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(false)
                            .message("근무 시간 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Get working hours by schedule name
     */
    @Operation(summary = "스케줄명으로 근무 시간 조회", description = "스케줄 이름으로 근무 시간 설정을 조회합니다")
    @GetMapping("/schedule/{scheduleName}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER', 'HR_MANAGER', 'USER')")
    public ResponseEntity<ApiResponse<WorkingHoursEntity>> getWorkingHoursByScheduleName(
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId,
            @Parameter(description = "스케줄명", required = true, example = "표준 근무시간")
            @PathVariable String scheduleName) {
        try {
            log.debug("GET /api/working-hours/schedule/{} - tenantId: {}", scheduleName, tenantId);
            Optional<WorkingHoursEntity> workingHours =
                    workingHoursService.findByTenantIdAndScheduleName(tenantId, scheduleName);
            if (workingHours.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.<WorkingHoursEntity>builder()
                                .success(true)
                                .data(workingHours.get())
                                .message("근무 시간 조회 성공")
                                .build()
                );
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.<WorkingHoursEntity>builder()
                                .success(false)
                                .message("근무 시간을 찾을 수 없습니다")
                                .errorCode(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                                .build()
                );
            }
        } catch (Exception e) {
            log.error("Failed to get working hours by schedule name", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(false)
                            .message("근무 시간 조회 실패: " + e.getMessage())
                            .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Create working hours
     */
    @Operation(summary = "근무 시간 생성", description = "새로운 근무 시간 설정을 생성합니다")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<WorkingHoursEntity>> createWorkingHours(
            @Parameter(description = "근무 시간 정보", required = true)
            @Valid @RequestBody WorkingHoursEntity workingHours) {
        try {
            log.info("POST /api/working-hours - scheduleName: {}", workingHours.getScheduleName());
            WorkingHoursEntity created = workingHoursService.createWorkingHours(workingHours);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(true)
                            .data(created)
                            .message("근무 시간 생성 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create working hours", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(false)
                            .message("근무 시간 생성 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Update working hours
     */
    @Operation(summary = "근무 시간 수정", description = "근무 시간 설정을 수정합니다")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<WorkingHoursEntity>> updateWorkingHours(
            @Parameter(description = "근무 시간 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "근무 시간 정보", required = true)
            @Valid @RequestBody WorkingHoursEntity workingHours) {
        try {
            log.info("PUT /api/working-hours/{} - scheduleName: {}", id, workingHours.getScheduleName());
            workingHours.setWorkingHoursId(id);
            WorkingHoursEntity updated = workingHoursService.updateWorkingHours(workingHours);
            return ResponseEntity.ok(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(true)
                            .data(updated)
                            .message("근무 시간 수정 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to update working hours", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(false)
                            .message("근무 시간 수정 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Delete working hours
     */
    @Operation(summary = "근무 시간 삭제", description = "근무 시간 설정을 삭제합니다")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteWorkingHours(
            @Parameter(description = "근무 시간 ID", required = true)
            @PathVariable Long id) {
        try {
            log.info("DELETE /api/working-hours/{}", id);
            workingHoursService.deleteWorkingHours(id);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .success(true)
                            .message("근무 시간 삭제 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to delete working hours", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<Void>builder()
                            .success(false)
                            .message("근무 시간 삭제 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }

    /**
     * Set as default working hours
     */
    @Operation(summary = "기본 근무 시간 설정", description = "특정 근무 시간을 기본값으로 설정합니다")
    @PutMapping("/{id}/set-default")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'HR_MANAGER')")
    public ResponseEntity<ApiResponse<WorkingHoursEntity>> setAsDefault(
            @Parameter(description = "근무 시간 ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "테넌트 ID", required = true)
            @RequestParam String tenantId) {
        try {
            log.info("PUT /api/working-hours/{}/set-default - tenantId: {}", id, tenantId);
            WorkingHoursEntity updated = workingHoursService.setAsDefault(id, tenantId);
            return ResponseEntity.ok(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(true)
                            .data(updated)
                            .message("기본 근무 시간 설정 성공")
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to set as default working hours", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponse.<WorkingHoursEntity>builder()
                            .success(false)
                            .message("기본 근무 시간 설정 실패: " + e.getMessage())
                            .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                            .build()
            );
        }
    }
}
