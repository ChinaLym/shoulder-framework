package demo1.ex;

import org.shoulder.core.exception.BaseRuntimeException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public class MyEx2 extends BaseRuntimeException {

    public MyEx2(String code, String message) {
        super(code, message);
    }

    {
        // 这类异常是由用户传参错误引起的，要默认记录 error 级别日志，返回 400 错误码
        super.setLogLevel(Level.WARN);
        super.setHttpStatus(HttpStatus.BAD_REQUEST);
    }
}