package kr.co.softice.mes.common.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Tenant Interceptor
 * HTTP 헤더에서 Tenant ID를 추출하여 TenantContext에 저장
 *
 * @author Moon Myung-seop
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {

    @Value("${app.tenant.header-name:X-Tenant-ID}")
    private String tenantHeaderName;

    @Value("${app.tenant.default-tenant:softice}")
    private String defaultTenant;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Check if TenantContext is already set (e.g., by JwtAuthenticationFilter)
        String existingTenantId = TenantContext.getCurrentTenant();
        if (existingTenantId != null && !existingTenantId.trim().isEmpty()) {
            log.debug("Tenant context already set to: {}", existingTenantId);
            return true;
        }

        // Extract from header if not already set
        String tenantId = request.getHeader(tenantHeaderName);

        if (tenantId == null || tenantId.trim().isEmpty()) {
            log.debug("No tenant header found, using default: {}", defaultTenant);
            tenantId = defaultTenant;
        }

        log.debug("Extracted tenant ID: {}", tenantId);
        TenantContext.setCurrentTenant(tenantId);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // Optional: Add tenant ID to response header
        response.setHeader(tenantHeaderName, TenantContext.getCurrentTenant());
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // Clean up tenant context
        TenantContext.clear();
    }
}
