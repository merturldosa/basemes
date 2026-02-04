package kr.co.softice.mes.common.dto.process;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Process Create Request DTO
 * 공정 생성 요청
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessCreateRequest {

    @NotBlank(message = "공정 코드는 필수입니다")
    @Size(max = 50, message = "공정 코드는 50자 이하여야 합니다")
    private String processCode;

    @NotBlank(message = "공정명은 필수입니다")
    @Size(max = 200, message = "공정명은 200자 이하여야 합니다")
    private String processName;

    @Size(max = 50, message = "공정 유형은 50자 이하여야 합니다")
    private String processType;

    private Integer sequenceOrder;  // 공정 순서

    @Size(max = 1000, message = "비고는 1000자 이하여야 합니다")
    private String remarks;
}
