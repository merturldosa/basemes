package kr.co.softice.mes.common.dto.downtime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Downtime Create Request DTO
 * 비가동 생성 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DowntimeCreateRequest {

    @NotNull(message = "설비 ID는 필수입니다.")
    private Long equipmentId;

    @NotBlank(message = "비가동 코드는 필수입니다.")
    private String downtimeCode;

    @NotBlank(message = "비가동 유형은 필수입니다.")
    private String downtimeType; // BREAKDOWN, SETUP_CHANGE, MATERIAL_SHORTAGE, QUALITY_ISSUE, PLANNED_MAINTENANCE, UNPLANNED_MAINTENANCE, NO_ORDER, OTHER

    private String downtimeCategory;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long workOrderId;
    private Long operationId;

    private Long responsibleUserId;
    private String responsibleName;

    private String cause;
    private String countermeasure;
    private String preventiveAction;

    private String remarks;
}
