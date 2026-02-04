package kr.co.softice.mes.common.dto.routing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Routing Response DTO
 * 공정 라우팅 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingResponse {

    private Long routingId;
    private String tenantId;
    private String tenantName;
    private Long productId;
    private String productCode;
    private String productName;
    private String routingCode;
    private String routingName;
    private String version;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private Boolean isActive;
    private Integer totalStandardTime;
    private String remarks;
    private List<RoutingStepResponse> steps;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
