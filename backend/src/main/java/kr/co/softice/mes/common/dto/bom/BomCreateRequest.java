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
 * BOM Create Request DTO
 * BOM 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BomCreateRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "BOM code is required")
    private String bomCode;

    @NotBlank(message = "BOM name is required")
    private String bomName;

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
    @NotEmpty(message = "At least one BOM detail is required")
    private List<BomDetailRequest> details;
}
