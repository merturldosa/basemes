package kr.co.softice.mes.common.dto.routing;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Routing Create Request DTO
 * 공정 라우팅 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingCreateRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Routing code is required")
    private String routingCode;

    @NotBlank(message = "Routing name is required")
    private String routingName;

    @NotBlank(message = "Version is required")
    @Builder.Default
    private String version = "1.0";

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @Builder.Default
    private Boolean isActive = true;

    private String remarks;

    @Valid
    @NotEmpty(message = "At least one routing step is required")
    private List<RoutingStepRequest> steps;
}
