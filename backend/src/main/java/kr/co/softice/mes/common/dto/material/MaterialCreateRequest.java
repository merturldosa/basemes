package kr.co.softice.mes.common.dto.material;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Material Create Request DTO
 * 자재 생성 요청 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCreateRequest {

    @NotBlank(message = "자재 코드는 필수입니다")
    private String materialCode;

    @NotBlank(message = "자재명은 필수입니다")
    private String materialName;

    @NotBlank(message = "자재 유형은 필수입니다")
    private String materialType;  // RAW_MATERIAL, SUB_MATERIAL, SEMI_FINISHED, FINISHED_PRODUCT

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
    private Boolean lotManaged;
    private Integer shelfLifeDays;

    // Status
    private Boolean isActive;

    // Additional Info
    private String remarks;
}
