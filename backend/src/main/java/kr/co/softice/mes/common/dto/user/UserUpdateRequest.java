package kr.co.softice.mes.common.dto.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Update Request DTO
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;

    @Size(max = 100, message = "이름은 최대 100자까지 가능합니다")
    private String fullName;

    @Size(max = 10, message = "선호 언어 코드는 최대 10자까지 가능합니다")
    private String preferredLanguage;
}
