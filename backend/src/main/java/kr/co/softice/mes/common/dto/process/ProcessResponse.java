package kr.co.softice.mes.common.dto.process;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Process Response DTO
 * 공정 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessResponse {

    private Long processId;
    private String processCode;
    private String processName;
    private String processType;
    private Integer sequenceOrder;
    private Boolean isActive;
    private String tenantId;
    private String tenantName;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
