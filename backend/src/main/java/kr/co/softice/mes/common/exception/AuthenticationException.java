package kr.co.softice.mes.common.exception;

/**
 * Authentication Exception
 * 인증 실패 시 발생
 *
 * @author Moon Myung-seop
 */
public class AuthenticationException extends BusinessException {

    public AuthenticationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthenticationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
