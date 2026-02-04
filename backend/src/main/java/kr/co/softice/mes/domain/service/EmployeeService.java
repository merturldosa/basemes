package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.employee.EmployeeRequest;
import kr.co.softice.mes.common.dto.employee.EmployeeResponse;
import kr.co.softice.mes.domain.entity.DepartmentEntity;
import kr.co.softice.mes.domain.entity.EmployeeEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.DepartmentRepository;
import kr.co.softice.mes.domain.repository.EmployeeRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public List<EmployeeResponse> getAllEmployees(String tenantId) {
        TenantEntity tenant = getTenant(tenantId);
        return employeeRepository.findByTenant(tenant).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<EmployeeResponse> getEmployees(String tenantId, Pageable pageable) {
        TenantEntity tenant = getTenant(tenantId);
        return employeeRepository.findByTenant(tenant, pageable).map(this::toResponse);
    }

    public Page<EmployeeResponse> searchEmployees(String tenantId, String keyword, Pageable pageable) {
        TenantEntity tenant = getTenant(tenantId);
        return employeeRepository.searchByKeyword(tenant, keyword, pageable).map(this::toResponse);
    }

    @Transactional
    public EmployeeResponse createEmployee(String tenantId, EmployeeRequest request, String username) {
        TenantEntity tenant = getTenant(tenantId);
        
        if (employeeRepository.existsByTenantAndEmployeeNo(tenant, request.getEmployeeNo())) {
            throw new RuntimeException("이미 존재하는 사원 코드입니다");
        }

        EmployeeEntity.EmployeeEntityBuilder builder = EmployeeEntity.builder()
                .tenant(tenant)
                .employeeNo(request.getEmployeeNo())
                .employeeName(request.getEmployeeName())
                .position(request.getPosition())
                .jobGrade(request.getJobGrade())
                .hireDate(request.getHireDate())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .birthDate(request.getBirthDate())
                .gender(request.getGender())
                .address(request.getAddress())
                .addressDetail(request.getAddressDetail())
                .postalCode(request.getPostalCode())
                .emergencyContact(request.getEmergencyContact())
                .emergencyContactRelation(request.getEmergencyContactRelation())
                .employmentStatus(request.getEmploymentStatus())
                .isActive(request.getIsActive())
                .createdBy(username)
                .updatedBy(username);

        if (request.getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("부서를 찾을 수 없습니다"));
            builder.department(department);
        }

        if (request.getUserId() != null) {
            UserEntity user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
            builder.user(user);
        }

        EmployeeEntity employee = employeeRepository.save(builder.build());
        return toResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(String tenantId, Long id, EmployeeRequest request, String username) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사원을 찾을 수 없습니다"));
        
        employee.setEmployeeNo(request.getEmployeeNo());
        employee.setEmployeeName(request.getEmployeeName());
        employee.setPosition(request.getPosition());
        employee.setJobGrade(request.getJobGrade());
        employee.setHireDate(request.getHireDate());
        employee.setPhoneNumber(request.getPhoneNumber());
        employee.setEmail(request.getEmail());
        employee.setBirthDate(request.getBirthDate());
        employee.setGender(request.getGender());
        employee.setAddress(request.getAddress());
        employee.setAddressDetail(request.getAddressDetail());
        employee.setPostalCode(request.getPostalCode());
        employee.setEmergencyContact(request.getEmergencyContact());
        employee.setEmergencyContactRelation(request.getEmergencyContactRelation());
        employee.setEmploymentStatus(request.getEmploymentStatus());
        employee.setIsActive(request.getIsActive());
        employee.setUpdatedBy(username);

        if (request.getDepartmentId() != null) {
            DepartmentEntity department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("부서를 찾을 수 없습니다"));
            employee.setDepartment(department);
        }
        
        return toResponse(employee);
    }

    @Transactional
    public void deleteEmployee(String tenantId, Long id) {
        EmployeeEntity employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사원을 찾을 수 없습니다"));
        employeeRepository.delete(employee);
    }

    private EmployeeResponse toResponse(EmployeeEntity entity) {
        EmployeeResponse.EmployeeResponseBuilder builder = EmployeeResponse.builder()
                .id(entity.getEmployeeId())
                .employeeNo(entity.getEmployeeNo())
                .employeeName(entity.getEmployeeName())
                .position(entity.getPosition())
                .jobGrade(entity.getJobGrade())
                .hireDate(entity.getHireDate())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .birthDate(entity.getBirthDate())
                .gender(entity.getGender())
                .address(entity.getAddress())
                .addressDetail(entity.getAddressDetail())
                .postalCode(entity.getPostalCode())
                .emergencyContact(entity.getEmergencyContact())
                .emergencyContactRelation(entity.getEmergencyContactRelation())
                .resignationDate(entity.getResignationDate())
                .employmentStatus(entity.getEmploymentStatus())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getDepartment() != null) {
            builder.departmentId(entity.getDepartment().getDepartmentId())
                   .departmentName(entity.getDepartment().getDepartmentName());
        }

        if (entity.getUser() != null) {
            builder.userId(entity.getUser().getUserId())
                   .username(entity.getUser().getUsername());
        }

        return builder.build();
    }

    private TenantEntity getTenant(String tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("테넌트를 찾을 수 없습니다"));
    }
}
