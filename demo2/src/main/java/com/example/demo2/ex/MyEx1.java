package demo1.ex;

import org.shoulder.core.exception.BaseRuntimeException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

public class MyEx1 extends BaseRuntimeException {

    public MyEx1(String code, String message) {
        super(code, message);
    }

    {
        // 这类异常是本不该出现的，要默认记录 error 级别日志，返回 500 错误码
        super.setLogLevel(Level.ERROR);
        super.setHttpStatus(HttpStatus.SERVICE_UNAVAILABLE);
    }
}