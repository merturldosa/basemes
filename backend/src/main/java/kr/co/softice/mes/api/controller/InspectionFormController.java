package kr.co.softice.mes.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import kr.co.softice.mes.common.dto.equipment.InspectionFormCreateRequest;
import kr.co.softice.mes.common.dto.equipment.InspectionFormUpdateRequest;
import kr.co.softice.mes.common.dto.equipment.InspectionFormResponse;
import kr.co.softice.mes.common.dto.equipment.InspectionFormFieldDTO;
import kr.co.softice.mes.domain.entity.InspectionFormEntity;
import kr.co.softice.mes.domain.entity.InspectionFormFieldEntity;
import kr.co.softice.mes.domain.service.InspectionFormService;
import kr.co.softice.mes.common.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Inspection Form Controller
 * 점검 양식 컨트롤러
 * @author Moon Myung-seop
 */
@RestController
@RequestMapping("/api/inspection-forms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "InspectionForm", description = "점검 양식 API")
public class InspectionFormController {

    private final InspectionFormService inspectionFormService;

    /**
     * Get all inspection forms
     */
    @Transactional(readOnly = true)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검 양식 목록 조회", description = "모든 점검 양식을 조회합니다.")
    public ResponseEntity<List<InspectionFormResponse>> getAllForms() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting all inspection forms for tenant: {}", tenantId);

        List<InspectionFormEntity> forms = inspectionFormService.getAllForms(tenantId);
        List<InspectionFormResponse> response = forms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get active inspection forms
     */
    @Transactional(readOnly = true)
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "활성 점검 양식 목록 조회", description = "활성 상태의 점검 양식을 조회합니다.")
    public ResponseEntity<List<InspectionFormResponse>> getActiveForms() {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Getting active inspection forms for tenant: {}", tenantId);

        List<InspectionFormEntity> forms = inspectionFormService.getActiveForms(tenantId);
        List<InspectionFormResponse> response = forms.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Get inspection form by ID
     */
    @Transactional(readOnly = true)
    @GetMapping("/{formId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "점검 양식 상세 조회", description = "ID로 점검 양식을 조회합니다.")
    public ResponseEntity<InspectionFormResponse> getFormById(@PathVariable Long formId) {
        log.info("Getting inspection form by ID: {}", formId);

        InspectionFormEntity form = inspectionFormService.getFormById(formId);
        InspectionFormResponse response = toResponse(form);

        return ResponseEntity.ok(response);
    }

    /**
     * Create inspection form
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 양식 등록", description = "새로운 점검 양식을 등록합니다.")
    public ResponseEntity<InspectionFormResponse> createForm(@Valid @RequestBody InspectionFormCreateRequest request) {
        String tenantId = TenantContext.getCurrentTenant();
        log.info("Creating inspection form: {} for tenant: {}", request.getFormCode(), tenantId);

        InspectionFormEntity form = toEntity(request);
        InspectionFormEntity created = inspectionFormService.createForm(tenantId, form);
        InspectionFormResponse response = toResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update inspection form
     */
    @PutMapping("/{formId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 양식 수정", description = "점검 양식 정보를 수정합니다.")
    public ResponseEntity<InspectionFormResponse> updateForm(
            @PathVariable Long formId,
            @Valid @RequestBody InspectionFormUpdateRequest request) {
        log.info("Updating inspection form ID: {}", formId);

        InspectionFormEntity updateData = toEntity(request);
        InspectionFormEntity updated = inspectionFormService.updateForm(formId, updateData);
        InspectionFormResponse response = toResponse(updated);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete inspection form
     */
    @DeleteMapping("/{formId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EQUIPMENT_MANAGER')")
    @Operation(summary = "점검 양식 삭제", description = "점검 양식을 삭제합니다.")
    public ResponseEntity<Void> deleteForm(@PathVariable Long formId) {
        log.info("Deleting inspection form ID: {}", formId);

        inspectionFormService.deleteForm(formId);

        return ResponseEntity.ok().build();
    }

    /**
     * Convert InspectionFormCreateRequest to Entity
     */
    private InspectionFormEntity toEntity(InspectionFormCreateRequest request) {
        InspectionFormEntity form = InspectionFormEntity.builder()
                .formCode(request.getFormCode())
                .formName(request.getFormName())
                .description(request.getDescription())
                .equipmentType(request.getEquipmentType())
                .inspectionType(request.getInspectionType())
                .build();

        if (request.getFields() != null) {
            List<InspectionFormFieldEntity> fields = request.getFields().stream()
                    .map(dto -> {
                        InspectionFormFieldEntity field = InspectionFormFieldEntity.builder()
                                .fieldName(dto.getFieldName())
                                .fieldType(dto.getFieldType())
                                .fieldOrder(dto.getFieldOrder())
                                .isRequired(dto.getIsRequired())
                                .options(dto.getOptions())
                                .unit(dto.getUnit())
                                .minValue(dto.getMinValue())
                                .maxValue(dto.getMaxValue())
                                .build();
                        field.setForm(form);
                        return field;
                    })
                    .collect(Collectors.toList());
            form.setFields(fields);
        }

        return form;
    }

    /**
     * Convert InspectionFormUpdateRequest to Entity
     */
    private InspectionFormEntity toEntity(InspectionFormUpdateRequest request) {
        InspectionFormEntity form = InspectionFormEntity.builder()
                .formName(request.getFormName())
                .description(request.getDescription())
                .equipmentType(request.getEquipmentType())
                .inspectionType(request.getInspectionType())
                .build();

        if (request.getFields() != null) {
            List<InspectionFormFieldEntity> fields = request.getFields().stream()
                    .map(dto -> {
                        InspectionFormFieldEntity field = InspectionFormFieldEntity.builder()
                                .fieldId(dto.getFieldId())
                                .fieldName(dto.getFieldName())
                                .fieldType(dto.getFieldType())
                                .fieldOrder(dto.getFieldOrder())
                                .isRequired(dto.getIsRequired())
                                .options(dto.getOptions())
                                .unit(dto.getUnit())
                                .minValue(dto.getMinValue())
                                .maxValue(dto.getMaxValue())
                                .build();
                        field.setForm(form);
                        return field;
                    })
                    .collect(Collectors.toList());
            form.setFields(fields);
        }

        return form;
    }

    /**
     * Convert Entity to InspectionFormResponse
     */
    private InspectionFormResponse toResponse(InspectionFormEntity entity) {
        List<InspectionFormFieldDTO> fieldDTOs = null;
        if (entity.getFields() != null) {
            fieldDTOs = entity.getFields().stream()
                    .map(field -> InspectionFormFieldDTO.builder()
                            .fieldId(field.getFieldId())
                            .fieldName(field.getFieldName())
                            .fieldType(field.getFieldType())
                            .fieldOrder(field.getFieldOrder())
                            .isRequired(field.getIsRequired())
                            .options(field.getOptions())
                            .unit(field.getUnit())
                            .minValue(field.getMinValue())
                            .maxValue(field.getMaxValue())
                            .build())
                    .collect(Collectors.toList());
        }

        return InspectionFormResponse.builder()
                .formId(entity.getFormId())
                .tenantId(entity.getTenant().getTenantId())
                .tenantName(entity.getTenant().getTenantName())
                .formCode(entity.getFormCode())
                .formName(entity.getFormName())
                .description(entity.getDescription())
                .equipmentType(entity.getEquipmentType())
                .inspectionType(entity.getInspectionType())
                .fields(fieldDTOs)
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
