package sample.dividend.exception.imple;

import org.springframework.http.HttpStatus;
import sample.dividend.exception.AbstractException;

public class CompanyNotFoundException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "해당 티커와 일치하는 회사가 존재하지 않습니다.";
    }
}
