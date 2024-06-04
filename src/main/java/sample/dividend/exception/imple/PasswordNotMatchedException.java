package sample.dividend.exception.imple;

import org.springframework.http.HttpStatus;
import sample.dividend.exception.AbstractException;

public class PasswordNotMatchedException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }

    @Override
    public String getMessage() {
        return "비밀번호가 일치하지 않습니다.";
    }
}
