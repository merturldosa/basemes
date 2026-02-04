package kr.co.softice.mes.common.dto.process;

import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Process Update Request DTO
 * 공정 수정 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessUpdateRequest {

    @Size(max = 200, message = "공정명은 200자 이하여야 합니다")
    private String processName;

    @Size(max = 50, message = "공정 유형은 50자 이하여야 합니다")
    private String processType;

    private Integer sequenceOrder;

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
