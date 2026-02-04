package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.ProcessEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.ProcessRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ProcessService Unit Test
 *
 * 테스트 대상:
 * - 공정 CRUD
 * - 활성화/비활성화
 * - 중복 공정 검증
 * - 조회 기능
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProcessService 단위 테스트")
class ProcessServiceTest {

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private TenantRepository tenantRepository;

    @InjectMocks
    private ProcessService processService;

    private TenantEntity testTenant;
    private ProcessEntity testProcess;

    @BeforeEach
    void setUp() {
        // 테스트 테넌트
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        // 테스트 공정
        testProcess = new ProcessEntity();
        testProcess.setProcessId(1L);
        testProcess.setProcessCode("PROC-001");
        testProcess.setProcessName("조립");
        testProcess.setProcessType("ASSEMBLY");
        testProcess.setSequenceOrder(10);
        testProcess.setIsActive(true);
        testProcess.setTenant(testTenant);
    }

    // ================== 조회 테스트 ==================

    @Test
    @DisplayName("공정 조회 - 테넌트별 조회 성공")
    void testFindByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<ProcessEntity> expectedList = Arrays.asList(testProcess);

        when(processRepository.findByTenantIdWithTenantOrderBySequence(tenantId))
                .thenReturn(expectedList);

        // When
        List<ProcessEntity> result = processService.findByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProcessCode()).isEqualTo("PROC-001");
        verify(processRepository, times(1)).findByTenantIdWithTenantOrderBySequence(tenantId);
    }

    @Test
    @DisplayName("공정 조회 - 활성 공정만 조회 성공")
    void testFindActiveByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<ProcessEntity> expectedList = Arrays.asList(testProcess);

        when(processRepository.findByTenantIdAndIsActiveWithTenant(tenantId, true))
                .thenReturn(expectedList);

        // When
        List<ProcessEntity> result = processService.findActiveByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIsActive()).isTrue();
        verify(processRepository, times(1)).findByTenantIdAndIsActiveWithTenant(tenantId, true);
    }

    @Test
    @DisplayName("공정 조회 - ID로 조회 성공")
    void testFindById_Success() {
        // Given
        Long processId = 1L;
        when(processRepository.findById(processId))
                .thenReturn(Optional.of(testProcess));

        // When
        Optional<ProcessEntity> result = processService.findById(processId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProcessId()).isEqualTo(processId);
        verify(processRepository, times(1)).findById(processId);
    }

    @Test
    @DisplayName("공정 조회 - 공정 코드로 조회 성공")
    void testFindByProcessCode_Success() {
        // Given
        String tenantId = "TEST001";
        String processCode = "PROC-001";

        when(processRepository.findByTenant_TenantIdAndProcessCode(tenantId, processCode))
                .thenReturn(Optional.of(testProcess));

        // When
        Optional<ProcessEntity> result = processService.findByProcessCode(tenantId, processCode);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getProcessCode()).isEqualTo(processCode);
        verify(processRepository, times(1)).findByTenant_TenantIdAndProcessCode(tenantId, processCode);
    }

    // ================== 생성 테스트 ==================

    @Test
    @DisplayName("공정 생성 - 성공")
    void testCreateProcess_Success() {
        // Given
        when(processRepository.existsByTenantAndProcessCode(any(), anyString()))
                .thenReturn(false);

        when(processRepository.save(any(ProcessEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProcessEntity result = processService.createProcess(testProcess);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProcessCode()).isEqualTo("PROC-001");
        verify(processRepository, times(1)).existsByTenantAndProcessCode(any(), anyString());
        verify(processRepository, times(1)).save(any(ProcessEntity.class));
    }

    @Test
    @DisplayName("공정 생성 - 실패 (중복 공정 코드)")
    void testCreateProcess_Fail_Duplicate() {
        // Given
        when(processRepository.existsByTenantAndProcessCode(any(), anyString()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> processService.createProcess(testProcess))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Process code already exists")
                .hasMessageContaining("PROC-001");

        verify(processRepository, never()).save(any(ProcessEntity.class));
    }

    // ================== 수정 테스트 ==================

    @Test
    @DisplayName("공정 수정 - 성공")
    void testUpdateProcess_Success() {
        // Given
        when(processRepository.existsById(testProcess.getProcessId()))
                .thenReturn(true);

        when(processRepository.save(any(ProcessEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProcessEntity result = processService.updateProcess(testProcess);

        // Then
        assertThat(result).isNotNull();
        verify(processRepository, times(1)).existsById(testProcess.getProcessId());
        verify(processRepository, times(1)).save(any(ProcessEntity.class));
    }

    @Test
    @DisplayName("공정 수정 - 실패 (공정 없음)")
    void testUpdateProcess_Fail_NotFound() {
        // Given
        when(processRepository.existsById(testProcess.getProcessId()))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> processService.updateProcess(testProcess))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Process not found");

        verify(processRepository, never()).save(any(ProcessEntity.class));
    }

    // ================== 삭제 테스트 ==================

    @Test
    @DisplayName("공정 삭제 - 성공")
    void testDeleteProcess_Success() {
        // Given
        Long processId = 1L;
        doNothing().when(processRepository).deleteById(processId);

        // When
        processService.deleteProcess(processId);

        // Then
        verify(processRepository, times(1)).deleteById(processId);
    }

    // ================== 활성화/비활성화 테스트 ==================

    @Test
    @DisplayName("공정 활성화 - 성공")
    void testActivateProcess_Success() {
        // Given
        Long processId = 1L;
        testProcess.setIsActive(false);  // 비활성 상태

        when(processRepository.findById(processId))
                .thenReturn(Optional.of(testProcess));

        when(processRepository.save(any(ProcessEntity.class)))
                .thenAnswer(invocation -> {
                    ProcessEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isTrue();
                    return saved;
                });

        // When
        ProcessEntity result = processService.activateProcess(processId);

        // Then
        assertThat(result).isNotNull();
        verify(processRepository, times(1)).findById(processId);
        verify(processRepository, times(1)).save(any(ProcessEntity.class));
    }

    @Test
    @DisplayName("공정 활성화 - 실패 (공정 없음)")
    void testActivateProcess_Fail_NotFound() {
        // Given
        Long processId = 999L;

        when(processRepository.findById(processId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> processService.activateProcess(processId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Process not found")
                .hasMessageContaining("999");

        verify(processRepository, never()).save(any(ProcessEntity.class));
    }

    @Test
    @DisplayName("공정 비활성화 - 성공")
    void testDeactivateProcess_Success() {
        // Given
        Long processId = 1L;
        testProcess.setIsActive(true);  // 활성 상태

        when(processRepository.findById(processId))
                .thenReturn(Optional.of(testProcess));

        when(processRepository.save(any(ProcessEntity.class)))
                .thenAnswer(invocation -> {
                    ProcessEntity saved = invocation.getArgument(0);
                    assertThat(saved.getIsActive()).isFalse();
                    return saved;
                });

        // When
        ProcessEntity result = processService.deactivateProcess(processId);

        // Then
        assertThat(result).isNotNull();
        verify(processRepository, times(1)).findById(processId);
        verify(processRepository, times(1)).save(any(ProcessEntity.class));
    }

    @Test
    @DisplayName("공정 비활성화 - 실패 (공정 없음)")
    void testDeactivateProcess_Fail_NotFound() {
        // Given
        Long processId = 999L;

        when(processRepository.findById(processId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> processService.deactivateProcess(processId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Process not found")
                .hasMessageContaining("999");

        verify(processRepository, never()).save(any(ProcessEntity.class));
    }
}
