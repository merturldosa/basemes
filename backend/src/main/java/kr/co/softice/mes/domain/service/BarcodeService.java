package kr.co.softice.mes.domain.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.LotEntity;
import kr.co.softice.mes.domain.repository.LotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 바코드/QR 코드 생성 서비스
 * ZXing 라이브러리를 사용한 LOT QR 코드 생성
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BarcodeService {

    private final LotRepository lotRepository;

    // QR 코드 기본 설정
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;
    private static final String IMAGE_FORMAT = "PNG";

    /**
     * LOT QR 코드 생성 (Base64 인코딩)
     *
     * @param tenantId 테넌트 ID
     * @param lotId LOT ID
     * @return Base64 인코딩된 QR 코드 이미지
     */
    public String generateLotQRCode(String tenantId, Long lotId) {
        log.info("Generating QR code for LOT - Tenant: {}, LOT ID: {}", tenantId, lotId);

        // LOT 조회
        LotEntity lot = lotRepository.findByIdWithAllRelations(lotId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND));

        // 테넌트 검증
        if (!lot.getTenant().getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // QR 코드 데이터 생성 (JSON 형태)
        String qrData = buildLotQRData(lot);

        // QR 코드 이미지 생성
        return generateQRCodeImage(qrData);
    }

    /**
     * LOT 번호로 QR 코드 생성
     *
     * @param tenantId 테넌트 ID
     * @param lotNo LOT 번호
     * @return Base64 인코딩된 QR 코드 이미지
     */
    public String generateLotQRCodeByLotNo(String tenantId, String lotNo) {
        log.info("Generating QR code for LOT - Tenant: {}, LOT No: {}", tenantId, lotNo);

        // LOT 조회
        LotEntity lot = lotRepository.findByTenant_TenantIdAndLotNo(tenantId, lotNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND));

        // QR 코드 데이터 생성
        String qrData = buildLotQRData(lot);

        // QR 코드 이미지 생성
        return generateQRCodeImage(qrData);
    }

    /**
     * 일반 텍스트 QR 코드 생성
     *
     * @param data QR 코드에 인코딩할 데이터
     * @return Base64 인코딩된 QR 코드 이미지
     */
    public String generateQRCode(String data) {
        log.debug("Generating QR code for data: {}", data);
        return generateQRCodeImage(data);
    }

    /**
     * LOT QR 코드 데이터 빌드
     * JSON 형태로 LOT 정보 구성
     *
     * @param lot LOT 엔티티
     * @return QR 코드 데이터 문자열
     */
    private String buildLotQRData(LotEntity lot) {
        // QR 코드 데이터 포맷: 파싱 가능한 구조
        // 형식: LOT:{lotNo}|PRODUCT:{productCode}|QTY:{currentQuantity}|EXPIRY:{expiryDate}|STATUS:{qualityStatus}
        StringBuilder qrData = new StringBuilder();
        qrData.append("LOT:").append(lot.getLotNo());
        qrData.append("|PRODUCT:").append(lot.getProduct().getProductCode());
        qrData.append("|PRODUCT_NAME:").append(lot.getProduct().getProductName());
        qrData.append("|QTY:").append(lot.getCurrentQuantity());

        if (lot.getExpiryDate() != null) {
            qrData.append("|EXPIRY:").append(lot.getExpiryDate());
        }

        qrData.append("|STATUS:").append(lot.getQualityStatus());
        qrData.append("|UNIT:").append(lot.getProduct().getUnit());

        log.debug("LOT QR data: {}", qrData);
        return qrData.toString();
    }

    /**
     * QR 코드 이미지 생성 및 Base64 인코딩
     *
     * @param data QR 코드 데이터
     * @return Base64 인코딩된 PNG 이미지
     */
    private String generateQRCodeImage(String data) {
        try {
            // QR 코드 생성 옵션 설정
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 높은 오류 정정 수준
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1); // 여백 최소화

            // QR 코드 생성
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    data,
                    BarcodeFormat.QR_CODE,
                    QR_CODE_WIDTH,
                    QR_CODE_HEIGHT,
                    hints
            );

            // BufferedImage로 변환
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // PNG로 변환 후 Base64 인코딩
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, IMAGE_FORMAT, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            log.debug("QR code generated successfully - Size: {} bytes", imageBytes.length);

            // Data URI 형식으로 반환 (HTML <img> 태그에 직접 사용 가능)
            return "data:image/png;base64," + base64Image;

        } catch (WriterException e) {
            log.error("Failed to encode QR code data: {}", data, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            log.error("Failed to write QR code image", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * QR 코드 데이터 파싱
     * 스캔된 QR 코드에서 LOT 정보 추출
     *
     * @param qrData QR 코드 데이터
     * @return 파싱된 LOT 정보 맵
     */
    public Map<String, String> parseLotQRData(String qrData) {
        log.debug("Parsing LOT QR data: {}", qrData);

        Map<String, String> result = new HashMap<>();

        if (qrData == null || qrData.isEmpty()) {
            log.warn("Empty QR data");
            return result;
        }

        // 파이프(|)로 분리
        String[] parts = qrData.split("\\|");

        for (String part : parts) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        log.debug("Parsed LOT data: {}", result);
        return result;
    }

    /**
     * LOT 번호로 LOT 조회 (QR 코드 스캔 후 사용)
     *
     * @param tenantId 테넌트 ID
     * @param lotNo LOT 번호
     * @return LOT 엔티티
     */
    @Transactional(readOnly = true)
    public LotEntity getLotByQRScan(String tenantId, String lotNo) {
        log.info("Getting LOT by QR scan - Tenant: {}, LOT No: {}", tenantId, lotNo);

        return lotRepository.findByTenant_TenantIdAndLotNo(tenantId, lotNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOT_NOT_FOUND));
    }
}
