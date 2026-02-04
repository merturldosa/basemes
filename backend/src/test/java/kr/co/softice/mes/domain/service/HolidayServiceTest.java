package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.EntityNotFoundException;
import kr.co.softice.mes.domain.entity.HolidayEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.WorkingHoursEntity;
import kr.co.softice.mes.domain.repository.HolidayRepository;
import kr.co.softice.mes.domain.repository.WorkingHoursRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Holiday Service Test
 * 휴일 관리 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("휴일 서비스 테스트")
class HolidayServiceTest {

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private WorkingHoursRepository workingHoursRepository;

    @InjectMocks
    private HolidayService holidayService;

    private TenantEntity testTenant;
    private HolidayEntity testHoliday;
    private WorkingHoursEntity testWorkingHours;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);

        testHoliday = new HolidayEntity();
        testHoliday.setHolidayId(1L);
        testHoliday.setTenant(testTenant);
        testHoliday.setHolidayName("New Year's Day");
        testHoliday.setHolidayDate(LocalDate.of(2026, 1, 1));
        testHoliday.setHolidayType("NATIONAL");
        testHoliday.setIsActive(true);

        // Set working hours with only weekdays working
        testWorkingHours = WorkingHoursEntity.builder()
                .mondayStart(LocalTime.of(9, 0))
                .mondayEnd(LocalTime.of(18, 0))
                .tuesdayStart(LocalTime.of(9, 0))
                .tuesdayEnd(LocalTime.of(18, 0))
                .wednesdayStart(LocalTime.of(9, 0))
                .wednesdayEnd(LocalTime.of(18, 0))
                .thursdayStart(LocalTime.of(9, 0))
                .thursdayEnd(LocalTime.of(18, 0))
                .fridayStart(LocalTime.of(9, 0))
                .fridayEnd(LocalTime.of(18, 0))
                .saturdayStart(null)
                .saturdayEnd(null)
                .sundayStart(null)
                .sundayEnd(null)
                .build();
        testWorkingHours.setTenant(testTenant);
    }

    // === 조회 테스트 ===

    @Test
    @DisplayName("모든 휴일 조회 - 성공")
    void testFindAllHolidays_Success() {
        List<HolidayEntity> holidays = Arrays.asList(testHoliday);
        when(holidayRepository.findAllByTenantId(tenantId))
                .thenReturn(holidays);

        List<HolidayEntity> result = holidayService.findAllHolidays(tenantId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("활성 휴일 조회 - 성공")
    void testFindActiveHolidays_Success() {
        List<HolidayEntity> holidays = Arrays.asList(testHoliday);
        when(holidayRepository.findActiveByTenantId(tenantId))
                .thenReturn(holidays);

        List<HolidayEntity> result = holidayService.findActiveHolidays(tenantId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("연도별 휴일 조회 - 성공")
    void testFindHolidaysByYear_Success() {
        List<HolidayEntity> holidays = Arrays.asList(testHoliday);
        when(holidayRepository.findByTenantIdAndYear(tenantId, 2026))
                .thenReturn(holidays);

        List<HolidayEntity> result = holidayService.findHolidaysByYear(tenantId, 2026);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("날짜 범위로 휴일 조회 - 성공")
    void testFindHolidaysByDateRange_Success() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 12, 31);
        List<HolidayEntity> holidays = Arrays.asList(testHoliday);
        when(holidayRepository.findByTenantIdAndDateRange(tenantId, start, end))
                .thenReturn(holidays);

        List<HolidayEntity> result = holidayService.findHolidaysByDateRange(tenantId, start, end);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("타입별 휴일 조회 - 성공")
    void testFindHolidaysByType_Success() {
        List<HolidayEntity> holidays = Arrays.asList(testHoliday);
        when(holidayRepository.findByTenantIdAndHolidayType(tenantId, "NATIONAL"))
                .thenReturn(holidays);

        List<HolidayEntity> result = holidayService.findHolidaysByType(tenantId, "NATIONAL");

        assertThat(result).hasSize(1);
    }

    // === 생성 테스트 ===

    @Test
    @DisplayName("휴일 생성 - 성공")
    void testCreateHoliday_Success() {
        when(holidayRepository.existsByTenantIdAndDate(tenantId, testHoliday.getHolidayDate()))
                .thenReturn(false);
        when(holidayRepository.save(testHoliday))
                .thenReturn(testHoliday);

        HolidayEntity result = holidayService.createHoliday(testHoliday);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("휴일 생성 - 실패 (중복 날짜)")
    void testCreateHoliday_Fail_Duplicate() {
        when(holidayRepository.existsByTenantIdAndDate(tenantId, testHoliday.getHolidayDate()))
                .thenReturn(true);

        assertThatThrownBy(() -> holidayService.createHoliday(testHoliday))
                .isInstanceOf(BusinessException.class);
    }

    // === 수정 테스트 ===

    @Test
    @DisplayName("휴일 수정 - 성공")
    void testUpdateHoliday_Success() {
        testHoliday.setHolidayName("Updated Holiday");
        when(holidayRepository.findById(1L))
                .thenReturn(Optional.of(testHoliday));
        when(holidayRepository.save(any(HolidayEntity.class)))
                .thenReturn(testHoliday);

        HolidayEntity result = holidayService.updateHoliday(testHoliday);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("휴일 수정 - 실패 (존재하지 않음)")
    void testUpdateHoliday_Fail_NotFound() {
        when(holidayRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> holidayService.updateHoliday(testHoliday))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 삭제 테스트 ===

    @Test
    @DisplayName("휴일 삭제 - 성공")
    void testDeleteHoliday_Success() {
        when(holidayRepository.findById(1L))
                .thenReturn(Optional.of(testHoliday));

        holidayService.deleteHoliday(1L);

        verify(holidayRepository).delete(testHoliday);
    }

    @Test
    @DisplayName("휴일 삭제 - 실패 (존재하지 않음)")
    void testDeleteHoliday_Fail_NotFound() {
        when(holidayRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> holidayService.deleteHoliday(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === 업무일 확인 테스트 ===

    @Test
    @DisplayName("업무일 확인 - 평일 (업무일)")
    void testIsBusinessDay_Weekday() {
        LocalDate monday = LocalDate.of(2026, 1, 5); // Monday
        when(holidayRepository.isHoliday(tenantId, monday))
                .thenReturn(false);

        boolean result = holidayService.isBusinessDay(tenantId, monday);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("업무일 확인 - 주말 (비업무일)")
    void testIsBusinessDay_Weekend() {
        LocalDate saturday = LocalDate.of(2026, 1, 3); // Saturday
        when(holidayRepository.isHoliday(tenantId, saturday))
                .thenReturn(false);
        when(workingHoursRepository.findDefaultByTenantId(tenantId))
                .thenReturn(Optional.of(testWorkingHours));

        boolean result = holidayService.isBusinessDay(tenantId, saturday);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("업무일 확인 - 휴일 (비업무일)")
    void testIsBusinessDay_Holiday() {
        LocalDate holiday = LocalDate.of(2026, 1, 1);
        when(holidayRepository.isHoliday(tenantId, holiday))
                .thenReturn(true);

        boolean result = holidayService.isBusinessDay(tenantId, holiday);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("업무일 확인 - 토요일이 근무일로 설정됨")
    void testIsBusinessDay_SaturdayWorking() {
        LocalDate saturday = LocalDate.of(2026, 1, 3);
        WorkingHoursEntity saturdayWorkingHours = WorkingHoursEntity.builder()
                .saturdayStart(LocalTime.of(9, 0))
                .saturdayEnd(LocalTime.of(13, 0))
                .build();

        when(holidayRepository.isHoliday(tenantId, saturday))
                .thenReturn(false);
        when(workingHoursRepository.findDefaultByTenantId(tenantId))
                .thenReturn(Optional.of(saturdayWorkingHours));

        boolean result = holidayService.isBusinessDay(tenantId, saturday);

        assertThat(result).isTrue();
    }

    // === 업무일 계산 테스트 ===

    @Test
    @DisplayName("업무일 수 계산 - 평일만")
    void testCalculateBusinessDays() {
        LocalDate start = LocalDate.of(2026, 1, 5); // Monday
        LocalDate end = LocalDate.of(2026, 1, 9);   // Friday

        when(holidayRepository.isHoliday(eq(tenantId), any(LocalDate.class)))
                .thenReturn(false);

        long result = holidayService.calculateBusinessDays(tenantId, start, end);

        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("업무일 수 계산 - 주말 포함")
    void testCalculateBusinessDays_WithWeekend() {
        LocalDate start = LocalDate.of(2026, 1, 5);  // Monday
        LocalDate end = LocalDate.of(2026, 1, 11);   // Sunday

        when(holidayRepository.isHoliday(eq(tenantId), any(LocalDate.class)))
                .thenReturn(false);
        when(workingHoursRepository.findDefaultByTenantId(tenantId))
                .thenReturn(Optional.of(testWorkingHours));

        long result = holidayService.calculateBusinessDays(tenantId, start, end);

        assertThat(result).isEqualTo(5L); // Only weekdays
    }

    @Test
    @DisplayName("N 업무일 후 날짜 계산")
    void testAddBusinessDays() {
        LocalDate start = LocalDate.of(2026, 1, 5); // Monday

        when(holidayRepository.isHoliday(eq(tenantId), any(LocalDate.class)))
                .thenReturn(false);
        when(workingHoursRepository.findDefaultByTenantId(tenantId))
                .thenReturn(Optional.of(testWorkingHours));

        LocalDate result = holidayService.addBusinessDays(tenantId, start, 5);

        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 12)); // Next Monday
    }

    @Test
    @DisplayName("다음 업무일 조회 - 평일")
    void testGetNextBusinessDay_Weekday() {
        LocalDate monday = LocalDate.of(2026, 1, 5);

        when(holidayRepository.isHoliday(eq(tenantId), any(LocalDate.class)))
                .thenReturn(false);

        LocalDate result = holidayService.getNextBusinessDay(tenantId, monday);

        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 6)); // Tuesday
    }

    @Test
    @DisplayName("다음 업무일 조회 - 금요일")
    void testGetNextBusinessDay_Friday() {
        LocalDate friday = LocalDate.of(2026, 1, 9);

        when(holidayRepository.isHoliday(eq(tenantId), any(LocalDate.class)))
                .thenReturn(false);
        when(workingHoursRepository.findDefaultByTenantId(tenantId))
                .thenReturn(Optional.of(testWorkingHours));

        LocalDate result = holidayService.getNextBusinessDay(tenantId, friday);

        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 12)); // Next Monday
    }

    @Test
    @DisplayName("이전 업무일 조회 - 평일")
    void testGetPreviousBusinessDay_Weekday() {
        LocalDate tuesday = LocalDate.of(2026, 1, 6);

        when(holidayRepository.isHoliday(eq(tenantId), any(LocalDate.class)))
                .thenReturn(false);

        LocalDate result = holidayService.getPreviousBusinessDay(tenantId, tuesday);

        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 5)); // Monday
    }

    @Test
    @DisplayName("이전 업무일 조회 - 월요일")
    void testGetPreviousBusinessDay_Monday() {
        LocalDate monday = LocalDate.of(2026, 1, 5);

        when(holidayRepository.isHoliday(eq(tenantId), any(LocalDate.class)))
                .thenReturn(false);
        when(workingHoursRepository.findDefaultByTenantId(tenantId))
                .thenReturn(Optional.of(testWorkingHours));

        LocalDate result = holidayService.getPreviousBusinessDay(tenantId, monday);

        assertThat(result).isEqualTo(LocalDate.of(2026, 1, 2)); // Previous Friday
    }
}
