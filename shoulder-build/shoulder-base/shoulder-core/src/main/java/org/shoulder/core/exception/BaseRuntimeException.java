package org.shoulder.core.exception;

import org.shoulder.core.util.ExceptionUtil;
import org.springframework.boot.logging.LogLevel;

/**
 * 基础通用异常【一般不会发生的问题，一旦发生，通常需要人工排查和修复】
 * 抛出该异常代表服务器无法继续处理或完成业务处理，错误信息无法体现业务场景，这类异常不需要让用户知道细节和产生错误的原因。
 * 服务端记录 error 日志并触发告警，返回 500 HTTP 状态码， body 为 {"code":"0x12345678"} 这种，UI层一般直接提示为服务器异常，请稍后再试（选择性展示错误码xxx，调用链xxx）
 * 场景举例：json序列化异常、加解密异常、调用其他服务接口异常、数据库连接失败、
 * 推荐：非统一拦截捕获的异常使用子类，而非直接抛该类
 * 在Controller层或service层校检或捕获到错误或异常时，可直接抛出 BaseRuntimeException
 * 会通过统一异常处理将该错误信息捕获封装返回
 * <p>
 * 获取并填充异常信息 {@link ExceptionUtil#generateExceptionMessage}
 *
 * @author lym
 */
public class BaseRuntimeException extends RuntimeException implements ErrorCode {

    /**
     * 错误码
     */
    private String code;

    /**
     * 参数，用于填充 message。message 支持{}、%s这种
     */
    private Object[] args;

    private LogLevel logLevel;



    // ==================== 当且仅当发生在编码错误时发生 =================

    public BaseRuntimeException(Throwable cause) {
        super(cause);
    }

    public BaseRuntimeException(String message) {
        super(message);
    }

    public BaseRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    // ==================== 推荐使用带错误码的 =================


    /**
     * 核心接口
     *
     * @param code    错误码
     * @param message 错误描述
     */
    public BaseRuntimeException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BaseRuntimeException(String code, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.code = code;
        this.setArgs(args);
    }

    public BaseRuntimeException(String code, String message, Object... args) {
        super(message);
        this.code = code;
        this.setArgs(args);
    }


    public BaseRuntimeException(ErrorCode error) {
        this(error.getCode(), error.getMessage());
    }

    public BaseRuntimeException(ErrorCode error, Object... args) {
        this(error.getCode(), error.getMessage(), args);
    }

    @Override
    public String getCode() {
        return code;
    }

    protected void setCode(String code) {
        this.code = code;
    }

    @Override
    public Object[] getArgs() {
        return args == null ? null : args.clone();
    }

    @Override
    public void setArgs(Object... args) {
        this.args = args == null ? null : args.clone();
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

}
