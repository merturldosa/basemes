package kr.co.softice.mes.common.dto.material;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Material Response DTO
 * 자재 응답 DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponse {

    private Long materialId;
    private String tenantId;
    private String tenantName;
    private String materialCode;
    private String materialName;
    private String materialType;
    private String specification;
    private String model;
    private String unit;

    // Pricing
    private BigDecimal standardPrice;
    private BigDecimal currentPrice;
    private String currency;

    // Supplier
    private Long supplierId;
    private String supplierCode;
    private String supplierName;
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

    // Audit Fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
