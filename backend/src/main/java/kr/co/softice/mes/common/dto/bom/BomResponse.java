package kr.co.softice.mes.common.dto.bom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BOM Response DTO
 * BOM 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomResponse {

    private Long bomId;
    private String tenantId;
    private String tenantName;
    private Long productId;
    private String productCode;
    private String productName;
    private String bomCode;
    private String bomName;
    private String version;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private Boolean isActive;
    private String remarks;
    private List<BomDetailResponse> details;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
