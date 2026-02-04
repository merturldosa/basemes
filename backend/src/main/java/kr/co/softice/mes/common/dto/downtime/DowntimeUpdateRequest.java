package kr.co.softice.mes.common.dto.downtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Downtime Update Request DTO
 * 비가동 수정 요청 DTO
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DowntimeUpdateRequest {

    private LocalDateTime endTime;

    private String downtimeType;
    private String downtimeCategory;

    private String cause;
    private String countermeasure;
    private String preventiveAction;

    private String remarks;
}
