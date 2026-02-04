package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * LOT QR 코드 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotQRResponse {

    /**
     * LOT ID
     */
    private Long lotId;

    /**
     * LOT 번호
     */
    private String lotNo;

    /**
     * QR 코드 이미지 (Base64 인코딩, Data URI 형식)
     * 예: "data:image/png;base64,iVBORw0KGgo..."
     */
    private String qrCodeImage;

    /**
     * QR 코드 데이터 (원본 텍스트)
     */
    private String qrData;
}
