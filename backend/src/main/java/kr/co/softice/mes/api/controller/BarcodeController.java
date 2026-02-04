package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.wms.LotQRResponse;
import kr.co.softice.mes.common.dto.wms.QRScanRequest;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.service.BarcodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 바코드/QR 코드 컨트롤러
 * LOT QR 코드 생성 및 스캔 API 제공
 *
 * @author Moon Myung-seop
 */
@Slf4j
@RestController
@RequestMapping("/api/barcodes")
@RequiredArgsConstructor
@Tag(name = "Barcode", description = "바코드/QR 코드 API")
public class BarcodeController {

    private final BarcodeService barcodeService;

    /**
     * LOT QR 코드 생성 (LOT ID 기준)
     */
    @GetMapping("/lot/{lotId}/qrcode")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "LOT QR 코드 생성", description = "LOT ID로 QR 코드 이미지 생성 (Base64)")
    public ResponseEntity<ApiResponse<LotQRResponse>> generateLotQRCode(@PathVariable Long lotId) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("LOT QR code generation request - Tenant: {}, LOT ID: {}", tenantId, lotId);

        String qrCodeBase64 = barcodeService.generateLotQRCode(tenantId, lotId);

        LotQRResponse response = LotQRResponse.builder()
                .lotId(lotId)
                .qrCodeImage(qrCodeBase64)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("QR 코드 생성 완료", response)
        );
    }

    /**
     * LOT QR 코드 생성 (LOT 번호 기준)
     */
    @GetMapping("/lot/number/{lotNo}/qrcode")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "LOT 번호로 QR 코드 생성", description = "LOT 번호로 QR 코드 이미지 생성 (Base64)")
    public ResponseEntity<ApiResponse<LotQRResponse>> generateLotQRCodeByLotNo(@PathVariable String lotNo) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("LOT QR code generation request - Tenant: {}, LOT No: {}", tenantId, lotNo);

        String qrCodeBase64 = barcodeService.generateLotQRCodeByLotNo(tenantId, lotNo);

        LotQRResponse response = LotQRResponse.builder()
                .lotNo(lotNo)
                .qrCodeImage(qrCodeBase64)
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("QR 코드 생성 완료", response)
        );
    }

    /**
     * QR 코드 스캔 (LOT 정보 조회)
     */
    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_MANAGER', 'WAREHOUSE_OPERATOR')")
    @Operation(summary = "QR 코드 스캔", description = "스캔된 QR 코드 데이터로 LOT 정보 조회")
    public ResponseEntity<ApiResponse<LotScanResponse>> scanQRCode(
            @Valid @RequestBody QRScanRequest request) {

        String tenantId = TenantContext.getCurrentTenant();

        log.info("QR code scan request - Tenant: {}, Data length: {}",
                tenantId, request.getQrData().length());

        // QR 데이터 파싱
        Map<String, String> parsedData = barcodeService.parseLotQRData(request.getQrData());
        String lotNo = parsedData.get("LOT");

        if (lotNo == null || lotNo.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("유효하지 않은 QR 코드입니다")
            );
        }

        // LOT 조회
        LotEntity lot = barcodeService.getLotByQRScan(tenantId, lotNo);

        LotScanResponse response = LotScanResponse.builder()
                .lotId(lot.getLotId())
                .lotNo(lot.getLotNo())
                .productCode(lot.getProduct().getProductCode())
                .productName(lot.getProduct().getProductName())
                .currentQuantity(lot.getCurrentQuantity())
                .expiryDate(lot.getExpiryDate())
                .qualityStatus(lot.getQualityStatus())
                .unit(lot.getProduct().getUnit())
                .isActive(lot.getIsActive())
                .build();

        return ResponseEntity.ok(
                ApiResponse.success("LOT 정보 조회 완료", response)
        );
    }

    /**
     * 일반 텍스트 QR 코드 생성
     */
    @PostMapping("/qrcode/generate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "텍스트 QR 코드 생성", description = "임의의 텍스트로 QR 코드 생성")
    public ResponseEntity<ApiResponse<String>> generateQRCode(@RequestBody Map<String, String> request) {

        String data = request.get("data");
        if (data == null || data.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("데이터가 필요합니다")
            );
        }

        log.info("QR code generation request - Data: {}", data);

        String qrCodeBase64 = barcodeService.generateQRCode(data);

        return ResponseEntity.ok(
                ApiResponse.success("QR 코드 생성 완료", qrCodeBase64)
        );
    }

    /**
     * LOT 스캔 응답 DTO
     */
    @lombok.Getter
    @lombok.Builder
    private static class LotScanResponse {
        private Long lotId;
        private String lotNo;
        private String productCode;
        private String productName;
        private java.math.BigDecimal currentQuantity;
        private java.time.LocalDate expiryDate;
        private String qualityStatus;
        private String unit;
        private Boolean isActive;
    }
}
