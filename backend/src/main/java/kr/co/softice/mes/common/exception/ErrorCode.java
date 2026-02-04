package kr.co.softice.mes.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Error Code Enum
 * 모든 에러 코드 정의
 *
 * @author Moon Myung-seop
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common (1xxx)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C1000", "내부 서버 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C1001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C1002", "허용되지 않은 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C1003", "엔티티를 찾을 수 없습니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C1004", "잘못된 타입 값입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C1005", "접근이 거부되었습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C1006", "리소스를 찾을 수 없습니다."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "C1007", "유효성 검사에 실패했습니다."),
    INVALID_OPERATION(HttpStatus.BAD_REQUEST, "C1008", "잘못된 작업입니다."),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "C1009", "중복된 리소스입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "C1010", "잘못된 상태 전환입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C1011", "잘못된 입력입니다."),
    INSPECTION_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "C1012", "검사가 완료되지 않았습니다."),
    INSPECTION_FAILED(HttpStatus.BAD_REQUEST, "C1013", "검사에 실패했습니다."),
    CANNOT_CANCEL_SHIPPED(HttpStatus.BAD_REQUEST, "C1014", "이미 출하된 항목은 취소할 수 없습니다."),

    // Authentication & Authorization (2xxx)
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "A2000", "인증에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A2001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A2002", "만료된 토큰입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A2003", "잘못된 인증 정보입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A2004", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A2005", "권한이 없습니다."),

    // Tenant (3xxx)
    TENANT_NOT_FOUND(HttpStatus.NOT_FOUND, "T3000", "테넌트를 찾을 수 없습니다."),
    TENANT_ALREADY_EXISTS(HttpStatus.CONFLICT, "T3001", "이미 존재하는 테넌트입니다."),
    TENANT_CONTEXT_NOT_SET(HttpStatus.BAD_REQUEST, "T3002", "테넌트 컨텍스트가 설정되지 않았습니다."),

    // User (4xxx)
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U4000", "사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U4001", "이미 존재하는 사용자입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U4002", "이미 사용 중인 이메일입니다."),
    USER_NOT_ACTIVE(HttpStatus.FORBIDDEN, "U4003", "활성화되지 않은 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U4004", "잘못된 비밀번호입니다."),
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "U4005", "비밀번호가 일치하지 않습니다."),

    // Role (5xxx)
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "R5000", "역할을 찾을 수 없습니다."),
    ROLE_ALREADY_EXISTS(HttpStatus.CONFLICT, "R5001", "이미 존재하는 역할입니다."),

    // Permission (6xxx)
    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "P6000", "권한을 찾을 수 없습니다."),
    PERMISSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "P6001", "이미 존재하는 권한입니다."),
    PERMISSION_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "P6002", "이미 할당된 권한입니다."),

    // Code (7xxx)
    CODE_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "CG7000", "코드 그룹을 찾을 수 없습니다."),
    CODE_GROUP_ALREADY_EXISTS(HttpStatus.CONFLICT, "CG7001", "이미 존재하는 코드 그룹입니다."),
    CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "CD7100", "코드를 찾을 수 없습니다."),
    CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "CD7101", "이미 존재하는 코드입니다."),

    // Theme (8xxx)
    THEME_NOT_FOUND(HttpStatus.NOT_FOUND, "TH8000", "테마를 찾을 수 없습니다."),
    THEME_ALREADY_EXISTS(HttpStatus.CONFLICT, "TH8001", "이미 존재하는 테마입니다."),
    INVALID_THEME_CONFIG(HttpStatus.BAD_REQUEST, "TH8002", "잘못된 테마 설정입니다."),

    // Production (9xxx)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR9000", "제품을 찾을 수 없습니다."),
    PRODUCT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PR9001", "이미 존재하는 제품입니다."),
    PROCESS_NOT_FOUND(HttpStatus.NOT_FOUND, "PC9100", "공정을 찾을 수 없습니다."),
    PROCESS_ALREADY_EXISTS(HttpStatus.CONFLICT, "PC9101", "이미 존재하는 공정입니다."),
    WORK_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "WO9200", "작업 지시를 찾을 수 없습니다."),
    WORK_ORDER_ALREADY_EXISTS(HttpStatus.CONFLICT, "WO9201", "이미 존재하는 작업 지시입니다."),
    WORK_RESULT_NOT_FOUND(HttpStatus.NOT_FOUND, "WR9300", "작업 실적을 찾을 수 없습니다."),

    // Quality Management (10xxx)
    QUALITY_STANDARD_NOT_FOUND(HttpStatus.NOT_FOUND, "QS10000", "품질 기준을 찾을 수 없습니다."),
    QUALITY_STANDARD_ALREADY_EXISTS(HttpStatus.CONFLICT, "QS10001", "이미 존재하는 품질 기준입니다."),
    QUALITY_INSPECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QI10100", "품질 검사를 찾을 수 없습니다."),
    QUALITY_INSPECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "QI10101", "이미 존재하는 품질 검사입니다."),

    // Inventory Management (11xxx)
    WAREHOUSE_NOT_FOUND(HttpStatus.NOT_FOUND, "WH11000", "창고를 찾을 수 없습니다."),
    WAREHOUSE_ALREADY_EXISTS(HttpStatus.CONFLICT, "WH11001", "이미 존재하는 창고입니다."),
    LOT_NOT_FOUND(HttpStatus.NOT_FOUND, "LT11100", "LOT을 찾을 수 없습니다."),
    LOT_ALREADY_EXISTS(HttpStatus.CONFLICT, "LT11101", "이미 존재하는 LOT입니다."),
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "IV11200", "재고를 찾을 수 없습니다."),
    INSUFFICIENT_INVENTORY(HttpStatus.BAD_REQUEST, "IV11201", "재고가 부족합니다."),
    INVENTORY_TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "IT11300", "재고 이동 내역을 찾을 수 없습니다."),
    INVENTORY_TRANSACTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "IT11301", "이미 존재하는 재고 이동 내역입니다."),

    // BOM Management (12xxx)
    BOM_NOT_FOUND(HttpStatus.NOT_FOUND, "BM12000", "BOM을 찾을 수 없습니다."),
    BOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "BM12001", "이미 존재하는 BOM입니다."),
    BOM_VERSION_ALREADY_EXISTS(HttpStatus.CONFLICT, "BM12002", "이미 존재하는 BOM 버전입니다."),
    BOM_DETAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "BD12100", "BOM 상세를 찾을 수 없습니다."),

    // Business Management - Customer/Supplier (13xxx)
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "CU13000", "고객을 찾을 수 없습니다."),
    CUSTOMER_ALREADY_EXISTS(HttpStatus.CONFLICT, "CU13001", "이미 존재하는 고객입니다."),
    SUPPLIER_NOT_FOUND(HttpStatus.NOT_FOUND, "SU13100", "공급업체를 찾을 수 없습니다."),
    SUPPLIER_ALREADY_EXISTS(HttpStatus.CONFLICT, "SU13101", "이미 존재하는 공급업체입니다."),

    // Material Management (14xxx)
    MATERIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "MT14000", "자재를 찾을 수 없습니다."),
    MATERIAL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MT14001", "이미 존재하는 자재입니다."),

    // Purchase Management (15xxx)
    PURCHASE_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "PR15000", "구매 요청을 찾을 수 없습니다."),
    PURCHASE_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, "PR15001", "이미 존재하는 구매 요청입니다."),
    PURCHASE_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "PO15100", "구매 주문을 찾을 수 없습니다."),
    PURCHASE_ORDER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PO15101", "이미 존재하는 구매 주문입니다."),
    PURCHASE_ORDER_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "PI15102", "구매 주문 항목을 찾을 수 없습니다."),
    GOODS_RECEIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "GR15200", "입하를 찾을 수 없습니다."),
    GOODS_RECEIPT_ALREADY_EXISTS(HttpStatus.CONFLICT, "GR15201", "이미 존재하는 입하입니다."),

    // Sales Management (16xxx)
    SALES_ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "SO16000", "판매 주문을 찾을 수 없습니다."),
    SALES_ORDER_ALREADY_EXISTS(HttpStatus.CONFLICT, "SO16001", "이미 존재하는 판매 주문입니다."),
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "DL16100", "출하를 찾을 수 없습니다."),
    DELIVERY_ALREADY_EXISTS(HttpStatus.CONFLICT, "DL16101", "이미 존재하는 출하입니다."),

    // Common Management (17xxx)
    SITE_NOT_FOUND(HttpStatus.NOT_FOUND, "ST17000", "사업장을 찾을 수 없습니다."),
    SITE_ALREADY_EXISTS(HttpStatus.CONFLICT, "ST17001", "이미 존재하는 사업장입니다."),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DP17100", "부서를 찾을 수 없습니다."),
    DEPARTMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "DP17101", "이미 존재하는 부서입니다."),
    EMPLOYEE_NOT_FOUND(HttpStatus.NOT_FOUND, "EM17200", "사원을 찾을 수 없습니다."),
    EMPLOYEE_ALREADY_EXISTS(HttpStatus.CONFLICT, "EM17201", "이미 존재하는 사원입니다."),
    HOLIDAY_NOT_FOUND(HttpStatus.NOT_FOUND, "HL17300", "휴일을 찾을 수 없습니다."),
    HOLIDAY_ALREADY_EXISTS(HttpStatus.CONFLICT, "HL17301", "이미 존재하는 휴일입니다."),
    APPROVAL_LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "AL17400", "결재라인을 찾을 수 없습니다."),
    APPROVAL_LINE_ALREADY_EXISTS(HttpStatus.CONFLICT, "AL17401", "이미 존재하는 결재라인입니다."),

    // Warehouse Operations (18xxx)
    SHIPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "SH18100", "출하를 찾을 수 없습니다."),
    SHIPPING_ALREADY_EXISTS(HttpStatus.CONFLICT, "SH18101", "이미 존재하는 출하입니다."),
    MATERIAL_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "MR18200", "불출 신청을 찾을 수 없습니다."),
    MATERIAL_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, "MR18201", "이미 존재하는 불출 신청입니다."),
    MATERIAL_HANDOVER_NOT_FOUND(HttpStatus.NOT_FOUND, "MH18300", "자재 인수인계를 찾을 수 없습니다."),
    MATERIAL_HANDOVER_ALREADY_EXISTS(HttpStatus.CONFLICT, "MH18301", "이미 존재하는 자재 인수인계입니다."),
    RETURN_NOT_FOUND(HttpStatus.NOT_FOUND, "RT18400", "반품을 찾을 수 없습니다."),
    RETURN_ALREADY_EXISTS(HttpStatus.CONFLICT, "RT18401", "이미 존재하는 반품입니다."),
    DISPOSAL_NOT_FOUND(HttpStatus.NOT_FOUND, "DS18500", "폐기를 찾을 수 없습니다."),
    DISPOSAL_ALREADY_EXISTS(HttpStatus.CONFLICT, "DS18501", "이미 존재하는 폐기입니다."),
    WEIGHING_NOT_FOUND(HttpStatus.NOT_FOUND, "WG18600", "칭량 기록을 찾을 수 없습니다."),
    WEIGHING_ALREADY_EXISTS(HttpStatus.CONFLICT, "WG18601", "이미 존재하는 칭량 기록입니다."),

    // Defect Management (19xxx)
    DEFECT_NOT_FOUND(HttpStatus.NOT_FOUND, "DF19000", "불량을 찾을 수 없습니다."),
    DEFECT_ALREADY_EXISTS(HttpStatus.CONFLICT, "DF19001", "이미 존재하는 불량입니다."),
    AFTER_SALES_NOT_FOUND(HttpStatus.NOT_FOUND, "AS19100", "A/S를 찾을 수 없습니다."),
    AFTER_SALES_ALREADY_EXISTS(HttpStatus.CONFLICT, "AS19101", "이미 존재하는 A/S입니다."),
    CLAIM_NOT_FOUND(HttpStatus.NOT_FOUND, "CL19200", "클레임을 찾을 수 없습니다."),
    CLAIM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CL19201", "이미 존재하는 클레임입니다."),

    // Equipment Management (20xxx)
    EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "EQ20000", "설비를 찾을 수 없습니다."),
    EQUIPMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "EQ20001", "이미 존재하는 설비입니다."),
    EQUIPMENT_OPERATION_NOT_FOUND(HttpStatus.NOT_FOUND, "EO20100", "설비 가동 이력을 찾을 수 없습니다."),
    EQUIPMENT_OPERATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "EO20101", "이미 존재하는 설비 가동 이력입니다."),
    EQUIPMENT_INSPECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "EI20200", "설비 점검을 찾을 수 없습니다."),
    EQUIPMENT_INSPECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "EI20201", "이미 존재하는 설비 점검입니다."),

    // Downtime Management (21xxx)
    DOWNTIME_NOT_FOUND(HttpStatus.NOT_FOUND, "DT21000", "비가동을 찾을 수 없습니다."),
    DOWNTIME_ALREADY_EXISTS(HttpStatus.CONFLICT, "DT21001", "이미 존재하는 비가동입니다."),

    // Mold Management (22xxx)
    MOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "MO22000", "금형을 찾을 수 없습니다."),
    MOLD_ALREADY_EXISTS(HttpStatus.CONFLICT, "MO22001", "이미 존재하는 금형입니다."),
    MOLD_MAINTENANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "MM22100", "금형 보전 이력을 찾을 수 없습니다."),
    MOLD_MAINTENANCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "MM22101", "이미 존재하는 금형 보전 이력입니다."),
    MOLD_PRODUCTION_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "MP22200", "금형 생산 이력을 찾을 수 없습니다."),

    // Employee Management - Skills (23xxx)
    SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, "SK23000", "스킬을 찾을 수 없습니다."),
    SKILL_ALREADY_EXISTS(HttpStatus.CONFLICT, "SK23001", "이미 존재하는 스킬입니다."),
    EMPLOYEE_SKILL_NOT_FOUND(HttpStatus.NOT_FOUND, "ES23100", "사원 스킬을 찾을 수 없습니다."),
    EMPLOYEE_SKILL_ALREADY_EXISTS(HttpStatus.CONFLICT, "ES23101", "이미 존재하는 사원 스킬입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
