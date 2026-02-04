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
 * Routing Update Request DTO
 * 공정 라우팅 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingUpdateRequest {

    @NotNull(message = "Routing ID is required")
    private Long routingId;

    @NotBlank(message = "Routing name is required")
    private String routingName;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private String remarks;

    @Valid
    @NotEmpty(message = "At least one routing step is required")
    private List<RoutingStepRequest> steps;
}
