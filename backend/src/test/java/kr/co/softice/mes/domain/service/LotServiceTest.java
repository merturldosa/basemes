package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.domain.entity.*;
import kr.co.softice.mes.domain.repository.LotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LotService Unit Test
 *
 * 테스트 대상:
 * - LOT 생성 (중복 검증)
 * - LOT 조회 (테넌트별, 제품별, 품질 상태별)
 * - LOT 업데이트
 * - LOT 삭제
 * - 품질 상태 업데이트
 *
 * @author Claude Sonnet 4.5
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LotService 단위 테스트")
class LotServiceTest {

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private LotService lotService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private LotEntity testLot;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        testTenant = new TenantEntity();
        testTenant.setTenantId("TEST001");
        testTenant.setTenantName("테스트 회사");

        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P-LCD-001");
        testProduct.setProductName("LCD 패널");
        testProduct.setUnit("EA");
        testProduct.setTenant(testTenant);

        testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT-2026-001");
        testLot.setProduct(testProduct);
        testLot.setTenant(testTenant);
        testLot.setInitialQuantity(new BigDecimal("1000"));
        testLot.setCurrentQuantity(new BigDecimal("1000"));
        testLot.setReservedQuantity(BigDecimal.ZERO);
        testLot.setQualityStatus("PASSED");
        testLot.setExpiryDate(LocalDate.now().plusYears(1));
        testLot.setCreatedAt(LocalDateTime.now());
        testLot.setIsActive(true);
    }

    @Test
    @DisplayName("LOT 생성 - 성공")
    void testCreateLot_Success() {
        // Given
        when(lotRepository.existsByTenantAndLotNo(any(TenantEntity.class), anyString()))
                .thenReturn(false);

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity saved = invocation.getArgument(0);
                    saved.setLotId(1L);
                    return saved;
                });

        when(lotRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testLot));

        // When
        LotEntity result = lotService.createLot(testLot);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLotId()).isEqualTo(1L);
        assertThat(result.getLotNo()).isEqualTo("LOT-2026-001");
        assertThat(result.getQualityStatus()).isEqualTo("PASSED");

        verify(lotRepository, times(1)).existsByTenantAndLotNo(any(), anyString());
        verify(lotRepository, times(1)).save(any(LotEntity.class));
        verify(lotRepository, times(1)).findByIdWithAllRelations(anyLong());
    }

    @Test
    @DisplayName("LOT 생성 - 실패 (중복 LOT 번호)")
    void testCreateLot_Fail_DuplicateLotNo() {
        // Given
        when(lotRepository.existsByTenantAndLotNo(any(TenantEntity.class), anyString()))
                .thenReturn(true);  // 이미 존재

        // When & Then
        assertThatThrownBy(() -> lotService.createLot(testLot))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lot number already exists")
                .hasMessageContaining("LOT-2026-001");

        // 저장이 호출되지 않아야 함
        verify(lotRepository, never()).save(any(LotEntity.class));
    }

    @Test
    @DisplayName("LOT 조회 - 테넌트별 조회 성공")
    void testFindByTenant_Success() {
        // Given
        String tenantId = "TEST001";
        List<LotEntity> expectedLots = Arrays.asList(testLot);

        when(lotRepository.findByTenantIdWithAllRelations(tenantId))
                .thenReturn(expectedLots);

        // When
        List<LotEntity> result = lotService.findByTenant(tenantId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLotNo()).isEqualTo("LOT-2026-001");
        verify(lotRepository, times(1)).findByTenantIdWithAllRelations(tenantId);
    }

    @Test
    @DisplayName("LOT 조회 - 제품별 조회 성공")
    void testFindByTenantAndProduct_Success() {
        // Given
        String tenantId = "TEST001";
        Long productId = 1L;
        List<LotEntity> expectedLots = Arrays.asList(testLot);

        when(lotRepository.findByTenant_TenantIdAndProduct_ProductId(tenantId, productId))
                .thenReturn(expectedLots);

        // When
        List<LotEntity> result = lotService.findByTenantAndProduct(tenantId, productId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getProductId()).isEqualTo(productId);
        verify(lotRepository, times(1))
                .findByTenant_TenantIdAndProduct_ProductId(tenantId, productId);
    }

    @Test
    @DisplayName("LOT 조회 - 품질 상태별 조회 성공")
    void testFindByTenantAndQualityStatus_Success() {
        // Given
        String tenantId = "TEST001";
        String qualityStatus = "PASSED";
        List<LotEntity> expectedLots = Arrays.asList(testLot);

        when(lotRepository.findByTenant_TenantIdAndQualityStatus(tenantId, qualityStatus))
                .thenReturn(expectedLots);

        // When
        List<LotEntity> result = lotService.findByTenantAndQualityStatus(tenantId, qualityStatus);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQualityStatus()).isEqualTo("PASSED");
        verify(lotRepository, times(1))
                .findByTenant_TenantIdAndQualityStatus(tenantId, qualityStatus);
    }

    @Test
    @DisplayName("LOT 조회 - ID로 조회 성공")
    void testFindById_Success() {
        // Given
        Long lotId = 1L;
        when(lotRepository.findByIdWithAllRelations(lotId))
                .thenReturn(Optional.of(testLot));

        // When
        Optional<LotEntity> result = lotService.findById(lotId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getLotId()).isEqualTo(lotId);
        verify(lotRepository, times(1)).findByIdWithAllRelations(lotId);
    }

    @Test
    @DisplayName("LOT 조회 - ID로 조회 실패 (존재하지 않음)")
    void testFindById_NotFound() {
        // Given
        Long lotId = 999L;
        when(lotRepository.findByIdWithAllRelations(lotId))
                .thenReturn(Optional.empty());

        // When
        Optional<LotEntity> result = lotService.findById(lotId);

        // Then
        assertThat(result).isEmpty();
        verify(lotRepository, times(1)).findByIdWithAllRelations(lotId);
    }

    @Test
    @DisplayName("LOT 조회 - LOT 번호로 조회 성공")
    void testFindByLotNo_Success() {
        // Given
        String tenantId = "TEST001";
        String lotNo = "LOT-2026-001";
        when(lotRepository.findByTenant_TenantIdAndLotNo(tenantId, lotNo))
                .thenReturn(Optional.of(testLot));

        // When
        Optional<LotEntity> result = lotService.findByLotNo(tenantId, lotNo);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getLotNo()).isEqualTo(lotNo);
        verify(lotRepository, times(1)).findByTenant_TenantIdAndLotNo(tenantId, lotNo);
    }

    @Test
    @DisplayName("LOT 업데이트 - 성공")
    void testUpdateLot_Success() {
        // Given
        testLot.setCurrentQuantity(new BigDecimal("800"));
        testLot.setReservedQuantity(new BigDecimal("200"));

        when(lotRepository.save(any(LotEntity.class)))
                .thenReturn(testLot);

        when(lotRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testLot));

        // When
        LotEntity result = lotService.updateLot(testLot);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCurrentQuantity()).isEqualByComparingTo("800");
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("200");

        verify(lotRepository, times(1)).save(any(LotEntity.class));
        verify(lotRepository, times(1)).findByIdWithAllRelations(anyLong());
    }

    @Test
    @DisplayName("LOT 삭제 - 성공")
    void testDeleteLot_Success() {
        // Given
        Long lotId = 1L;
        doNothing().when(lotRepository).deleteById(lotId);

        // When
        lotService.deleteLot(lotId);

        // Then
        verify(lotRepository, times(1)).deleteById(lotId);
    }

    @Test
    @DisplayName("품질 상태 업데이트 - 성공")
    void testUpdateQualityStatus_Success() {
        // Given
        Long lotId = 1L;
        String newQualityStatus = "FAILED";

        when(lotRepository.findById(lotId))
                .thenReturn(Optional.of(testLot));

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity saved = invocation.getArgument(0);
                    saved.setQualityStatus(newQualityStatus);
                    return saved;
                });

        when(lotRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(testLot));

        // When
        LotEntity result = lotService.updateQualityStatus(lotId, newQualityStatus);

        // Then
        assertThat(result).isNotNull();
        verify(lotRepository, times(1)).findById(lotId);
        verify(lotRepository, times(1)).save(any(LotEntity.class));
        verify(lotRepository, times(1)).findByIdWithAllRelations(anyLong());
    }

    @Test
    @DisplayName("품질 상태 업데이트 - 실패 (LOT 없음)")
    void testUpdateQualityStatus_Fail_LotNotFound() {
        // Given
        Long lotId = 999L;
        String newQualityStatus = "FAILED";

        when(lotRepository.findById(lotId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> lotService.updateQualityStatus(lotId, newQualityStatus))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lot not found")
                .hasMessageContaining("999");

        // 저장이 호출되지 않아야 함
        verify(lotRepository, never()).save(any(LotEntity.class));
    }

    @Test
    @DisplayName("LOT 생성 - 초기 데이터 검증")
    void testCreateLot_InitialDataValidation() {
        // Given
        LotEntity newLot = new LotEntity();
        newLot.setTenant(testTenant);
        newLot.setProduct(testProduct);
        newLot.setLotNo("LOT-2026-NEW");
        newLot.setInitialQuantity(new BigDecimal("500"));
        newLot.setCurrentQuantity(new BigDecimal("500"));
        newLot.setReservedQuantity(BigDecimal.ZERO);
        newLot.setQualityStatus("PENDING");

        when(lotRepository.existsByTenantAndLotNo(any(TenantEntity.class), anyString()))
                .thenReturn(false);

        when(lotRepository.save(any(LotEntity.class)))
                .thenAnswer(invocation -> {
                    LotEntity saved = invocation.getArgument(0);
                    saved.setLotId(2L);
                    return saved;
                });

        when(lotRepository.findByIdWithAllRelations(anyLong()))
                .thenReturn(Optional.of(newLot));

        // When
        LotEntity result = lotService.createLot(newLot);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLotNo()).isEqualTo("LOT-2026-NEW");
        assertThat(result.getInitialQuantity()).isEqualByComparingTo("500");
        assertThat(result.getCurrentQuantity()).isEqualByComparingTo("500");
        assertThat(result.getReservedQuantity()).isEqualByComparingTo("0");
        assertThat(result.getQualityStatus()).isEqualTo("PENDING");
    }
}
