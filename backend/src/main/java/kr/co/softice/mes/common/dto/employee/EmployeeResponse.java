package kr.co.softice.mes.common.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private Long id;
    private String employeeNo;
    private String employeeName;
    private Long departmentId;
    private String departmentName;
    private String position;
    private String jobGrade;
    private LocalDate hireDate;
    private String phoneNumber;
    private String email;
    private Long userId;
    private String username;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String addressDetail;
    private String postalCode;
    private String emergencyContact;
    private String emergencyContactRelation;
    private LocalDate resignationDate;
    private String employmentStatus;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
