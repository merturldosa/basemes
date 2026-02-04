package kr.co.softice.mes.common.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Login Trend Response DTO
 * 일별 로그인 추이 응답
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginTrendResponse {
    private LocalDate date;      // 날짜
    private Long loginCount;     // 로그인 수
    private String dateLabel;    // 표시용 날짜 레이블 (예: "01-19")
}
