package kr.co.softice.mes.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Audited Annotation
 * 감사 로그를 자동으로 기록할 메서드에 적용
 *
 * @author Moon Myung-seop
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audited {

    /**
     * 작업 유형
     * 예: CREATE, UPDATE, DELETE, LOGIN, LOGOUT
     */
    String action();

    /**
     * 작업 대상 엔티티 타입
     * 예: User, Role, Permission, Order
     */
    String entityType() default "";

    /**
     * 작업 설명 (SpEL 표현식 지원)
     * 예: "사용자 생성", "역할 #{#roleId}에 권한 할당"
     */
    String description() default "";

    /**
     * 변경 전 데이터 추적 여부
     */
    boolean trackOldValue() default false;

    /**
     * 변경 후 데이터 추적 여부
     */
    boolean trackNewValue() default false;
}
