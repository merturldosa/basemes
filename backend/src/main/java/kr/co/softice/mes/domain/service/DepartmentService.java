package kr.co.softice.mes.domain.service;

import kr.co.softice.mes.common.dto.department.DepartmentRequest;
import kr.co.softice.mes.common.dto.department.DepartmentResponse;
import kr.co.softice.mes.common.exception.BusinessException;
import kr.co.softice.mes.common.exception.ErrorCode;
import kr.co.softice.mes.domain.entity.DepartmentEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.repository.DepartmentRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
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
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TenantRepository tenantRepository;

    public List<DepartmentResponse> getAllDepartments(String tenantId) {
        TenantEntity tenant = getTenant(tenantId);
        return departmentRepository.findByTenantOrderBySortOrderAsc(tenant).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Page<DepartmentResponse> getDepartments(String tenantId, Pageable pageable) {
        TenantEntity tenant = getTenant(tenantId);
        return departmentRepository.findByTenant(tenant, pageable).map(this::toResponse);
    }

    @Transactional
    public DepartmentResponse createDepartment(String tenantId, DepartmentRequest request, String username) {
        TenantEntity tenant = getTenant(tenantId);
        
        if (departmentRepository.existsByTenantAndDepartmentCode(tenant, request.getDepartmentCode())) {
            throw new BusinessException(ErrorCode.DEPARTMENT_ALREADY_EXISTS, "이미 존재하는 부서 코드입니다: " + request.getDepartmentCode());
        }

        DepartmentEntity department = DepartmentEntity.builder()
                .tenant(tenant)
                .departmentCode(request.getDepartmentCode())
                .departmentName(request.getDepartmentName())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .createdBy(username)
                .updatedBy(username)
                .build();

        department = departmentRepository.save(department);
        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateDepartment(String tenantId, Long id, DepartmentRequest request, String username) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND, "부서를 찾을 수 없습니다: " + id));

        department.setDepartmentCode(request.getDepartmentCode());
        department.setDepartmentName(request.getDepartmentName());
        department.setDescription(request.getDescription());
        department.setSortOrder(request.getSortOrder());
        department.setIsActive(request.getIsActive());
        department.setUpdatedBy(username);
        
        return toResponse(department);
    }

    @Transactional
    public void deleteDepartment(String tenantId, Long id) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND, "부서를 찾을 수 없습니다: " + id));
        departmentRepository.delete(department);
    }

    private DepartmentResponse toResponse(DepartmentEntity entity) {
        return DepartmentResponse.builder()
                .id(entity.getDepartmentId())
                .departmentCode(entity.getDepartmentCode())
                .departmentName(entity.getDepartmentName())
                .description(entity.getDescription())
                .sortOrder(entity.getSortOrder())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private TenantEntity getTenant(String tenantId) {
        return tenantRepository.findById(tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TENANT_NOT_FOUND, "테넌트를 찾을 수 없습니다: " + tenantId));
    }
}
