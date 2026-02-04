package kr.co.softice.mes.common.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import kr.co.softice.mes.common.annotation.Audited;
import kr.co.softice.mes.common.security.TenantContext;
import kr.co.softice.mes.domain.entity.AuditLogEntity;
import kr.co.softice.mes.domain.entity.TenantEntity;
import kr.co.softice.mes.domain.entity.UserEntity;
import kr.co.softice.mes.domain.repository.AuditLogRepository;
import kr.co.softice.mes.domain.repository.TenantRepository;
import kr.co.softice.mes.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * Audit Aspect
 * @Audited 어노테이션이 붙은 메서드의 실행을 감사 로그로 기록
 *
 * @author Moon Myung-seop
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(kr.co.softice.mes.common.annotation.Audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Audited audited = method.getAnnotation(Audited.class);

        // 감사 로그 빌더 초기화
        AuditLogEntity.AuditLogEntityBuilder auditBuilder = AuditLogEntity.builder();

        // 현재 사용자 정보 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "anonymous";

        // HTTP 요청 정보 추출
        HttpServletRequest request = getCurrentHttpRequest();

        // 테넌트 정보 설정
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            tenantRepository.findById(tenantId).ifPresent(auditBuilder::tenant);
        }

        // 사용자 정보 설정
        if (!"anonymous".equals(username) && !"anonymousUser".equals(username)) {
            userRepository.findByUsername(username).ifPresent(user -> {
                auditBuilder.user(user);
                auditBuilder.username(user.getUsername());
            });
        } else {
            auditBuilder.username(username);
        }

        // 기본 감사 정보 설정
        auditBuilder.action(audited.action());
        auditBuilder.entityType(audited.entityType());
        auditBuilder.description(resolveDescription(audited.description(), joinPoint));

        // HTTP 요청 정보 설정
        if (request != null) {
            auditBuilder.ipAddress(getClientIp(request));
            auditBuilder.userAgent(request.getHeader("User-Agent"));
            auditBuilder.httpMethod(request.getMethod());
            auditBuilder.endpoint(request.getRequestURI());
        }

        // 변경 전 데이터 추적 (필요 시)
        Object oldValue = null;
        if (audited.trackOldValue()) {
            try {
                oldValue = extractOldValue(joinPoint);
                if (oldValue != null) {
                    auditBuilder.oldValue(objectMapper.writeValueAsString(oldValue));
                }
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize old value", e);
            }
        }

        // 메서드 실행
        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();

            // 엔티티 ID 추출 (결과에서)
            String entityId = extractEntityId(result);
            if (entityId != null) {
                auditBuilder.entityId(entityId);
            }

            // 변경 후 데이터 추적 (필요 시)
            if (audited.trackNewValue() && result != null) {
                try {
                    auditBuilder.newValue(objectMapper.writeValueAsString(result));
                } catch (JsonProcessingException e) {
                    log.warn("Failed to serialize new value", e);
                }
            }

        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            // 감사 로그 저장
            auditBuilder.success(success);
            auditBuilder.errorMessage(errorMessage);
            auditBuilder.createdAt(LocalDateTime.now());

            try {
                auditLogRepository.save(auditBuilder.build());
            } catch (Exception e) {
                log.error("Failed to save audit log", e);
            }
        }

        return result;
    }

    /**
     * SpEL 표현식을 사용한 설명 해석
     */
    private String resolveDescription(String description, ProceedingJoinPoint joinPoint) {
        if (description == null || description.isEmpty() || !description.contains("#{")) {
            return description;
        }

        try {
            EvaluationContext context = new StandardEvaluationContext();
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();

            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            return parser.parseExpression(description).getValue(context, String.class);
        } catch (Exception e) {
            log.warn("Failed to resolve description: {}", description, e);
            return description;
        }
    }

    /**
     * 변경 전 데이터 추출
     */
    private Object extractOldValue(ProceedingJoinPoint joinPoint) {
        // 첫 번째 파라미터가 ID인 경우, 해당 엔티티를 조회하여 반환
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long) {
            // 실제 구현에서는 엔티티 타입에 따라 적절한 repository에서 조회
            // 여기서는 간단히 null 반환
            return null;
        }
        return null;
    }

    /**
     * 결과에서 엔티티 ID 추출
     */
    private String extractEntityId(Object result) {
        if (result == null) {
            return null;
        }

        try {
            // getId() 메서드가 있으면 호출
            Method getIdMethod = result.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(result);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            // ID 추출 실패 시 무시
            return null;
        }
    }

    /**
     * 현재 HTTP 요청 가져오기
     */
    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 클라이언트 IP 주소 추출 (프록시 고려)
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
