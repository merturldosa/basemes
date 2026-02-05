package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.entity.ProductEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
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
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Barcode Service Test
 * 바코드/QR 코드 서비스 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("바코드 서비스 테스트")
class BarcodeServiceTest {

    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private BarcodeService barcodeService;

    private TenantEntity testTenant;
    private ProductEntity testProduct;
    private LotEntity testLot;
    private String tenantId;

    @BeforeEach
    void setUp() {
        tenantId = "TENANT001";

        testTenant = new TenantEntity();
        testTenant.setTenantId(tenantId);

        testProduct = new ProductEntity();
        testProduct.setProductId(1L);
        testProduct.setProductCode("P001");
        testProduct.setProductName("Test Product");
        testProduct.setUnit("EA");

        testLot = new LotEntity();
        testLot.setLotId(1L);
        testLot.setLotNo("LOT001");
        testLot.setProduct(testProduct);
        testLot.setTenant(testTenant);
        testLot.setCurrentQuantity(new BigDecimal("100"));
        testLot.setQualityStatus("OK");
        testLot.setExpiryDate(LocalDate.now().plusDays(30));
    }

    // === QR 코드 생성 테스트 ===

    @Test
    @DisplayName("LOT QR 코드 생성 by ID - 성공")
    void testGenerateLotQRCode_Success() {
        when(lotRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testLot));

        String result = barcodeService.generateLotQRCode(tenantId, 1L);

        assertThat(result).isNotNull();
        assertThat(result).startsWith("data:image/png;base64,");
        verify(lotRepository).findByIdWithAllRelations(1L);
    }

    @Test
    @DisplayName("LOT QR 코드 생성 - 실패 (LOT 없음)")
    void testGenerateLotQRCode_Fail_LotNotFound() {
        when(lotRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> barcodeService.generateLotQRCode(tenantId, 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("LOT QR 코드 생성 - 실패 (테넌트 불일치)")
    void testGenerateLotQRCode_Fail_TenantMismatch() {
        when(lotRepository.findByIdWithAllRelations(1L))
                .thenReturn(Optional.of(testLot));

        assertThatThrownBy(() -> barcodeService.generateLotQRCode("TENANT999", 1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("LOT QR 코드 생성 by LOT 번호 - 성공")
    void testGenerateLotQRCodeByLotNo_Success() {
        when(lotRepository.findByTenant_TenantIdAndLotNo(tenantId, "LOT001"))
                .thenReturn(Optional.of(testLot));

        String result = barcodeService.generateLotQRCodeByLotNo(tenantId, "LOT001");

        assertThat(result).isNotNull();
        assertThat(result).startsWith("data:image/png;base64,");
    }

    @Test
    @DisplayName("LOT QR 코드 생성 by LOT 번호 - 실패 (LOT 없음)")
    void testGenerateLotQRCodeByLotNo_Fail_LotNotFound() {
        when(lotRepository.findByTenant_TenantIdAndLotNo(tenantId, "LOT999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> barcodeService.generateLotQRCodeByLotNo(tenantId, "LOT999"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("일반 텍스트 QR 코드 생성 - 성공")
    void testGenerateQRCode_Success() {
        String result = barcodeService.generateQRCode("Test QR Code Data");

        assertThat(result).isNotNull();
        assertThat(result).startsWith("data:image/png;base64,");
    }

    // === QR 코드 데이터 파싱 테스트 ===

    @Test
    @DisplayName("LOT QR 데이터 파싱 - 성공")
    void testParseLotQRData_Success() {
        String qrData = "LOT:LOT001|PRODUCT:P001|PRODUCT_NAME:Test Product|QTY:100|EXPIRY:2026-02-26|STATUS:OK|UNIT:EA";

        Map<String, String> result = barcodeService.parseLotQRData(qrData);

        assertThat(result).isNotNull();
        assertThat(result.get("LOT")).isEqualTo("LOT001");
        assertThat(result.get("PRODUCT")).isEqualTo("P001");
        assertThat(result.get("PRODUCT_NAME")).isEqualTo("Test Product");
        assertThat(result.get("QTY")).isEqualTo("100");
        assertThat(result.get("STATUS")).isEqualTo("OK");
        assertThat(result.get("UNIT")).isEqualTo("EA");
    }

    @Test
    @DisplayName("LOT QR 데이터 파싱 - 빈 데이터")
    void testParseLotQRData_EmptyData() {
        Map<String, String> result = barcodeService.parseLotQRData("");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("LOT QR 데이터 파싱 - null 데이터")
    void testParseLotQRData_NullData() {
        Map<String, String> result = barcodeService.parseLotQRData(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("LOT QR 데이터 파싱 - 일부 필드만 있는 경우")
    void testParseLotQRData_PartialData() {
        String qrData = "LOT:LOT001|PRODUCT:P001";

        Map<String, String> result = barcodeService.parseLotQRData(qrData);

        assertThat(result).hasSize(2);
        assertThat(result.get("LOT")).isEqualTo("LOT001");
        assertThat(result.get("PRODUCT")).isEqualTo("P001");
    }

    // === LOT 조회 테스트 (QR 스캔 후) ===

    @Test
    @DisplayName("QR 스캔 후 LOT 조회 - 성공")
    void testGetLotByQRScan_Success() {
        when(lotRepository.findByTenant_TenantIdAndLotNo(tenantId, "LOT001"))
                .thenReturn(Optional.of(testLot));

        LotEntity result = barcodeService.getLotByQRScan(tenantId, "LOT001");

        assertThat(result).isNotNull();
        assertThat(result.getLotNo()).isEqualTo("LOT001");
    }

    @Test
    @DisplayName("QR 스캔 후 LOT 조회 - 실패 (LOT 없음)")
    void testGetLotByQRScan_Fail_LotNotFound() {
        when(lotRepository.findByTenant_TenantIdAndLotNo(tenantId, "LOT999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> barcodeService.getLotByQRScan(tenantId, "LOT999"))
                .isInstanceOf(BusinessException.class);
    }
}
