package kr.co.softice.mes.common.dto.material;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Material Update Request DTO
 * 자재 수정 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialUpdateRequest {

    @NotNull(message = "자재 ID는 필수입니다")
    private Long materialId;

    @NotBlank(message = "자재명은 필수입니다")
    private String materialName;

    @NotBlank(message = "자재 유형은 필수입니다")
    private String materialType;

    private String specification;
    private String model;

    @NotBlank(message = "단위는 필수입니다")
    private String unit;

    // Pricing
    private BigDecimal standardPrice;
    private BigDecimal currentPrice;
    private String currency;

    // Supplier
    private Long supplierId;
    private Integer leadTimeDays;

    // Stock Management
    private BigDecimal minStockQuantity;
    private BigDecimal maxStockQuantity;
    private BigDecimal safetyStockQuantity;
    private BigDecimal reorderPoint;

    // Storage
    private String storageLocation;

    // LOT Management
    @NotNull(message = "LOT 관리 여부는 필수입니다")
    private Boolean lotManaged;

    private Integer shelfLifeDays;

    // Status
    @NotNull(message = "활성화 여부는 필수입니다")
    private Boolean isActive;

    // Additional Info
    private String remarks;
}
