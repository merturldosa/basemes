package kr.co.softice.mes.common.security;

import lombok.extern.slf4j.Slf4j;

/**
 * Tenant Context
 * ThreadLocal을 사용하여 현재 요청의 Tenant ID를 저장
 *
 * @author Moon Myung-seop
 */
@Slf4j
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * 현재 Tenant ID 설정
     */
    public static void setCurrentTenant(String tenantId) {
        log.debug("Setting tenant context: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * 현재 Tenant ID 조회
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Tenant Context 초기화
     */
    public static void clear() {
        log.debug("Clearing tenant context");
        CURRENT_TENANT.remove();
    }
}
