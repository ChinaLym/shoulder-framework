package org.shoulder.core.constant;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.IError;

/**
 * 通用错误
 * @author lym
 */
public enum CommonErrorEnum implements IError {

    /**
     * 成功
     */
    SUCCESS(0, "success"),


    // ------------------------------- 认证 -----------------------------
    /**
     * 认证错误：登录失败, 如用户名或密码错误
     */
    AUTH_FAIL(1, "Authentication failed."),

    /**
     * 认证错误：服务认证失败,令牌错误
     */
    AUTH_TOKEN_ERROR(2, "Invalid token."),
    /**
     * 认证过期，需要重新认证
     */
    AUTH_EXPIRED(3, "Certification expired. Re-auth please."),


    // ------------------------------- 文件 -----------------------------
    /**
     * 文件系统错误：创建文件失败
     */
    FILE_CREATE_FAIL(10, "Failed to create the file."),
    /**
     * 文件系统错误：读文件失败
     */
    FILE_READ_FAIL(11, "Failed to write the file."),
    /**
     * 文件系统错误：写文件失败
     */
    FILE_WRITE_FAIL(12, "Failed to write the file."),
    /**
     * 文件系统错误：删除文件失败
     */
    FILE_DELETE_FAIL(13, "Failed to delete the file."),

    // ------------------------------- 网络 -----------------------------
    /**
     * 请求错误：指定的请求方法不能被服务器处理
     */
    REQUEST_METHOD_MISMATCH(20, "The specified request method cannot be processed by the server."),
    /**
     * 请求错误：请求超时
     */
    REQUEST_TIMEOUT(21, "Request timeout."),
    /**
     * 请求错误：实体格式不支持
     */
    REQUEST_BODY_INCORRECT(22, "Entity format not supported。"),


    // ------------------------------- 参数校验、HTTP请求不合格 -----------------------------
    /**
     * 参数校验未通过，参数非法，通过第三方校验框架抛出
     */
    PARAM_NOT_VALID(10000, "Parameter not valid. for %s"),
    /**
     * 参数不能为空
     */
    PARAM_BLANK(10000, "The required parameter %s is blank."),
    /**
     * 参数范围不正确，如年龄传入了负数
     */
    PARAM_OUT_RANGE(10000, "The value of parameter %s is not in the right range."),
    /**
     * 参数格式不正确
     */
    PARAM_FORMAT_INVALID(10000, "The format of parameter %s is not correct."),
    /**
     * 参数错误：返回报文过长，可能未指定分页大小 or 分页过大
     */
    PARAM_PAGE_SETTING_INVALID(10000, "Return message is too long, please resize page and retry."),
    /**
     * 参数不支持
     */
    PARAM_NOT_SUPPORT(10000, "The parameter(%s) not supported."),
    /**
     * 参数内容长度超长，如用户名最大允许32个字符
     */
    PARAM_CONTENT_TOO_LONG(10000, "The parameter(%s) content is out of limit."),
    /**
     * 包装 HttpMessageNotReadableException,
     * 请求体读取失败，如 Post 请求缺少参数或者解析 json 时失败了
     */
    PARAM_BODY_NOT_READABLE(10000, "HttpMessageNotReadable. %s"),
    /**
     * 参数类型不匹配
     */
    PARAM_TYPE_NOT_MATCH(10000, "MethodArgumentTypeMismatch. The value of %s(%s) resolved to %s fail."),
    /**
     * content-type 不正确
     */
    CONTENT_TYPE_INVALID(10000, "HttpMediaTypeNotSupported. ContentType(%s) is not acceptable."),
    /**
     * 文件上传出错
     */
    MULTIPART_INVALID(10000, "Request is not a validate multipart request, please check request or file size."),


    // ----------------------- 作为服务提供者 ----------------------

    /**
     * 未知异常
     */
    UNKNOWN(10000, "Unknown error."),
    /**
     * 响应超时
     */
    SERVICE_RESPONSE_TIMEOUT(10000, "Service response timeout."),
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(10000, "Service unavailable."),
    /**
     * 无效的安全会话 xSessionId 不正确 xSessionId in valid.
     */
    SECURITY_SESSION_INVALID(10000, "Security session invalid."),

    // ----------------------- 作为服务消费者 ----------------------
    RPC_UNKNOWN(1000, "RPC error with none error code or msg."),

    // ----------------------- 与中间件操作异常，代码正确时，常发于中间件宕机 ----------------------
    PERSISTENCE_TO_DB_FAIL(1000, "Persistent to database fail!"),

    ;

    private String code;

    private String message;

    CommonErrorEnum(String code) {
        this.code = code;
    }

    CommonErrorEnum(long code, String message) {
        this.code = Long.toHexString(code);
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public BaseRuntimeException toException(Object... args){
        return new BaseRuntimeException(this, args);
    }

    public BaseRuntimeException toException(Throwable t, Object... args){
        return new BaseRuntimeException(getCode(), getMessage(), t, args);
    }

    public void throwException(Object... args) throws BaseRuntimeException {
        throw toException(args);
    }

    public void throwException(Throwable t, Object... args) throws BaseRuntimeException {
        throw toException(t, args);
    }

}
