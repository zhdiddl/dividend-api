package sample.dividend.exception.imple;

import org.springframework.http.HttpStatus;
import sample.dividend.exception.AbstractException;

public class ScrapingFailedException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "배당금 정보 가져오기에 실패했습니다.";
    }
}
