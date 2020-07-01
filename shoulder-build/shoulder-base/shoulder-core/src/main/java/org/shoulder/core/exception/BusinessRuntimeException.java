package org.shoulder.core.exception;

/**
 * 业务场景中可预料到的异常【特定业务场景下意料之中的问题，一般不需要排查】
 * <p>
 * 异常发生在具体的业务场景，这类异常往往由使用者的输入直接或间接导致，因此需要让使用者知道错误产生原因与解决/避免的方法。
 * 服务端记录 warn 日志，返回 400 HTTP 状态码， body 为 {"code":"0x12345678", "msg":"用户名最长支持18个字符，当前%s个字符", "data":[20]} 这种，UI层一般不展示错误码，而是根据错误码或msg来进行提示
 * 场景举例：字段校验不通过，查询数据库中不存在的数据，不合法的枚举值等，
 * 推荐：非统一拦截捕获的异常使用子类，而非直接抛该类
 *
 * @author lym
 */
public class BusinessRuntimeException extends BaseRuntimeException implements ErrorCode {

    public BusinessRuntimeException(String code, String message) {
        super(code, message);
    }

    public BusinessRuntimeException(String code, String message, Throwable cause, Object... args) {
        super(code, message, cause, args);
    }

    public BusinessRuntimeException(String code, String message, Object... args) {
        super(code, message, args);
    }


    public BusinessRuntimeException(ErrorCode error) {
        this(error.getCode(), error.getMessage());
    }

    public BusinessRuntimeException(ErrorCode error, Object... args) {
        this(error.getCode(), error.getMessage(), args);
    }


}
