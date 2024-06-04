package sample.dividend.exception.imple;

import org.springframework.http.HttpStatus;
import sample.dividend.exception.AbstractException;

public class NoTickerException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "티커가 존재하지 않습니다.";
    }
}
