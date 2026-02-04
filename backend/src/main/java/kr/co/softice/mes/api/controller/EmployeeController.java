package kr.co.softice.mes.api.controller;

import javax.validation.Valid;
import kr.co.softice.mes.common.dto.ApiResponse;
import kr.co.softice.mes.common.dto.employee.EmployeeRequest;
import kr.co.softice.mes.common.dto.employee.EmployeeResponse;
import kr.co.softice.mes.domain.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees(
            @RequestHeader("X-Tenant-ID") String tenantId) {
        List<EmployeeResponse> employees = employeeService.getAllEmployees(tenantId);
        return ResponseEntity.ok(ApiResponse.success("사원 목록 조회 성공", employees));
    }

    @GetMapping("/page")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> getEmployees(
            @RequestHeader("X-Tenant-ID") String tenantId,
            Pageable pageable) {
        Page<EmployeeResponse> employees = employeeService.getEmployees(tenantId, pageable);
        return ResponseEntity.ok(ApiResponse.success("사원 페이징 조회 성공", employees));
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<EmployeeResponse>>> searchEmployees(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestParam String keyword,
            Pageable pageable) {
        Page<EmployeeResponse> employees = employeeService.searchEmployees(tenantId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success("사원 검색 성공", employees));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody EmployeeRequest request,
            Authentication authentication) {
        EmployeeResponse employee = employeeService.createEmployee(
                tenantId, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("사원 생성 성공", employee));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request,
            Authentication authentication) {
        EmployeeResponse employee = employeeService.updateEmployee(
                tenantId, id, request, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("사원 수정 성공", employee));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable Long id) {
        employeeService.deleteEmployee(tenantId, id);
        return ResponseEntity.ok(ApiResponse.success("사원 삭제 성공", null));
    }
}
