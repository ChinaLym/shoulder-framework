package org.shoulder.core.exception;

import org.shoulder.core.util.ExceptionUtil;

/**
 * 通用异常
 * 在Controller层或service层校检或捕获到错误或异常时，可直接抛出 BaseRuntimeException，
 *         会通过统一异常处理将该错误信息捕获封装返回
 *
 * 获取并填充异常信息 {@link ExceptionUtil#generateExceptionMessage}
 *
 * @author lym
 */
public class BaseRuntimeException extends RuntimeException implements IError {

    /** 错误码 */
    private String code;

    /** 参数，用于填充 message。message 支持{}、%s这种 */
    private Object[] args;

    // ==================== 当且仅当发生在编码错误时发生 =================

    public BaseRuntimeException(Throwable cause) {
        super(cause);
    }

    public BaseRuntimeException(String message){
        super(message);
    }

    public BaseRuntimeException(String message, Throwable cause){
        super(message, cause);
    }

    // ==================== 推荐使用带错误码的 =================


    /**
     * 核心接口
     * @param code      错误码
     * @param message   错误描述
     */
    public BaseRuntimeException(String code, String message){
        super(message);
        this.code = code;
    }

    public BaseRuntimeException(String code, String message, Throwable cause, Object... args){
        super(message,cause);
        this.code = code;
        if (null != args) {
            this.args = args.clone();
        }
    }

    public BaseRuntimeException(String code, String message, Object... args){
        super(message);
        this.code = code;
        if (null != args) {
            this.args = args.clone();
        }
    }


    public BaseRuntimeException(IError error){
        this(error.getCode(), error.getMessage());
    }

    public BaseRuntimeException(IError error, Object... args){
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
        if (null == args) {
            return null;
        } else {
            return  args.clone();
        }
    }

    @Override
    public void setArgs(Object... args) {
        if (null == args) {
            this.args = null;
        } else {
            this.args =  args.clone();
        }
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }

}
