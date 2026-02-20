package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.department.DepartmentRequest;
import kr.co.softice.mes.common.dto.department.DepartmentResponse;
import kr.co.softice.mes.domain.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Department", description = "부서 관리 API")
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments(
            @RequestHeader("X-Tenant-ID") String tenantId) {
        List<DepartmentResponse> departments = departmentService.getAllDepartments(tenantId);
        return ResponseEntity.ok(ApiResponse.success("부서 목록 조회 성공", departments));
    }

    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<DepartmentResponse>>> getDepartments(
            @RequestHeader("X-Tenant-ID") String tenantId,
            Pageable pageable) {
        Page<DepartmentResponse> departments = departmentService.getDepartments(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success("부서 페이징 조회 성공", departments));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody DepartmentRequest request,
            Authentication authentication) {
        DepartmentResponse department = departmentService.createDepartment(
                tenantId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("부서 생성 성공", department));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request,
            Authentication authentication) {
        DepartmentResponse department = departmentService.updateDepartment(
                tenantId, id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("부서 수정 성공", department));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable Long id) {
        departmentService.deleteDepartment(tenantId, id);
        return ResponseEntity.ok(ApiResponse.success("부서 삭제 성공", null));
    }
}
