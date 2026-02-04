package kr.co.softice.mes.common.dto.bom;

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
 * BOM Update Request DTO
 * BOM 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomUpdateRequest {

    @NotNull(message = "BOM ID is required")
    private Long bomId;

    @NotBlank(message = "BOM name is required")
    private String bomName;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveDate;

    private LocalDate expiryDate;

    @NotNull(message = "Active status is required")
    private Boolean isActive;

    private String remarks;

    @Valid
    @NotEmpty(message = "At least one BOM detail is required")
    private List<BomDetailRequest> details;
}
