package kr.co.softice.mes.common.dto.wms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * QR 코드 스캔 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRScanRequest {

    /**
     * 스캔된 QR 코드 데이터
     */
    @NotBlank(message = "QR 코드 데이터는 필수입니다")
    private String qrData;

    /**
     * 스캔 위치 (선택사항)
     * 예: "입하구역", "출하구역", "생산라인1"
     */
    private String scanLocation;

    /**
     * 스캔 목적 (선택사항)
     * 예: "RECEIVING", "SHIPPING", "INSPECTION", "INVENTORY_CHECK"
     */
    private String scanPurpose;
}
