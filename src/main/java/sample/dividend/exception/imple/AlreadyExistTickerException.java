package sample.dividend.exception.imple;

import org.springframework.http.HttpStatus;
import sample.dividend.exception.AbstractException;

public class AlreadyExistTickerException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이미 저장된 티커입니다.";
    }
}
