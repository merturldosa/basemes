package kr.co.softice.mes.common.exception;

/**
 * Duplicate Entity Exception
 * 중복된 엔티티가 존재할 때 발생
 *
 * @author Moon Myung-seop
 */
public class DuplicateEntityException extends BusinessException {

    public DuplicateEntityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DuplicateEntityException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
