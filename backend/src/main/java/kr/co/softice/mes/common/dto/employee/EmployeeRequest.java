package kr.co.softice.mes.common.dto.employee;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequest {

    @NotBlank(message = "사원 코드는 필수입니다")
    @Size(max = 20, message = "사원 코드는 20자를 초과할 수 없습니다")
    private String employeeNo;

    @NotBlank(message = "사원명은 필수입니다")
    @Size(max = 100, message = "사원명은 100자를 초과할 수 없습니다")
    private String employeeName;

    private Long departmentId;

    @Size(max = 50, message = "직위는 50자를 초과할 수 없습니다")
    private String position;

    @Size(max = 50, message = "직급은 50자를 초과할 수 없습니다")
    private String jobGrade;

    private LocalDate hireDate;

    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다")
    private String phoneNumber;

    @Email(message = "유효한 이메일 주소를 입력하세요")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String email;

    private Long userId;

    private LocalDate birthDate;

    @Size(max = 1, message = "성별은 M 또는 F만 가능합니다")
    private String gender;

    @Size(max = 200, message = "주소는 200자를 초과할 수 없습니다")
    private String address;

    @Size(max = 200, message = "상세 주소는 200자를 초과할 수 없습니다")
    private String addressDetail;

    @Size(max = 10, message = "우편번호는 10자를 초과할 수 없습니다")
    private String postalCode;

    @Size(max = 20, message = "비상 연락처는 20자를 초과할 수 없습니다")
    private String emergencyContact;

    @Size(max = 50, message = "비상 연락처 관계는 50자를 초과할 수 없습니다")
    private String emergencyContactRelation;

    private LocalDate resignationDate;

    @Size(max = 20, message = "재직 상태는 20자를 초과할 수 없습니다")
    @Builder.Default
    private String employmentStatus = "ACTIVE";

    @Builder.Default
    private Boolean isActive = true;
}
