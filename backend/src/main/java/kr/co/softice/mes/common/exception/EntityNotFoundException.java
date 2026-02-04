package kr.co.softice.mes.common.exception;

/**
 * Entity Not Found Exception
 * 엔티티를 찾을 수 없을 때 발생
 *
 * @author Moon Myung-seop
 */
public class EntityNotFoundException extends BusinessException {

    public EntityNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EntityNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
