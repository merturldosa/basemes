package kr.co.softice.mes.common.dto.defect;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Defect Create Request DTO
 * 불량 생성 요청
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectCreateRequest {

    @NotBlank(message = "불량 번호는 필수입니다")
    @Size(max = 50, message = "불량 번호는 50자 이하여야 합니다")
    private String defectNo;

    @NotNull(message = "불량 일시는 필수입니다")
    private LocalDateTime defectDate;

    @NotBlank(message = "발생 원천은 필수입니다")
    @Size(max = 30, message = "발생 원천은 30자 이하여야 합니다")
    private String sourceType; // PRODUCTION, RECEIVING, SHIPPING, INSPECTION, CUSTOMER

    // Optional source references
    private Long workOrderId;
    private Long workResultId;
    private Long goodsReceiptId;
    private Long shippingId;
    private Long qualityInspectionId;

    @NotNull(message = "제품 ID는 필수입니다")
    private Long productId;

    @Size(max = 50, message = "불량 유형은 50자 이하여야 합니다")
    private String defectType; // APPEARANCE, DIMENSION, FUNCTION, MATERIAL, ASSEMBLY, OTHER

    @Size(max = 50, message = "불량 분류는 50자 이하여야 합니다")
    private String defectCategory; // SCRATCH, CRACK, BURR, DEFORMATION, etc

    @Size(max = 200, message = "불량 위치는 200자 이하여야 합니다")
    private String defectLocation;

    private String defectDescription;

    @Builder.Default
    private BigDecimal defectQuantity = BigDecimal.ZERO;

    @Size(max = 100, message = "LOT 번호는 100자 이하여야 합니다")
    private String lotNo;

    @Size(max = 30, message = "심각도는 30자 이하여야 합니다")
    private String severity; // CRITICAL, MAJOR, MINOR

    @Builder.Default
    @Size(max = 30, message = "상태는 30자 이하여야 합니다")
    private String status = "REPORTED"; // REPORTED, IN_REVIEW, REWORK, SCRAP, CLOSED

    private Long responsibleDepartmentId;
    private Long responsibleUserId;
    private String rootCause;
    private String correctiveAction;
    private String preventiveAction;

    private Long reporterUserId;

    @Builder.Default
    private BigDecimal defectCost = BigDecimal.ZERO;

    private String remarks;
}
