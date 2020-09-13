package org.shoulder.core.exception;

import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;


/**
 * 通用错误（2^14以下框架使用）
 *
 * @author lym
 */
public enum CommonErrorCodeEnum implements ErrorCode {

    // ------------------------------- 认证 -----------------------------
    /**
     * 未认证，需要认证后才能访问
     */
    AUTH_401_NEED_AUTH(1, "Need Authentication.", Level.INFO, HttpStatus.UNAUTHORIZED),
    /**
     * 认证失败：如用户名或密码凭据无效、由于白名单等被拒绝、未开启匿名访问等
     */
    AUTH_401_UNAUTHORIZED(2, "Authentication failed.", Level.INFO, HttpStatus.UNAUTHORIZED),
    /**
     * 认证过期，需要重新认证
     */
    AUTH_401_EXPIRED(3, "Certification expired. Re-auth please.", Level.INFO, HttpStatus.UNAUTHORIZED),

    /**
     * 主动拒绝请求：权限不够
     */
    AUTH_403_FORBIDDEN(8, "Permission deny.", Level.INFO, HttpStatus.FORBIDDEN),
    /**
     * 主动拒绝请求：令牌无效
     */
    AUTH_403_TOKEN_INVALID(9, "Invalid token.", Level.INFO, HttpStatus.FORBIDDEN),


    // ------------------------------- 文件 -----------------------------
    /**
     * 文件系统错误：创建文件失败
     */
    FILE_CREATE_FAIL(10, "Failed to create the file.", Level.ERROR),
    /**
     * 文件系统错误：读文件失败
     */
    FILE_READ_FAIL(11, "Failed to write the file.", Level.ERROR),
    /**
     * 文件系统错误：写文件失败
     */
    FILE_WRITE_FAIL(12, "Failed to write the file.", Level.ERROR),
    /**
     * 文件系统错误：删除文件失败
     */
    FILE_DELETE_FAIL(13, "Failed to delete the file."),

    // ------------------------------- 发起网络请求时（作为服务消费者） -----------------------------

    /**
     * 未知异常，对方未遵循标准格式，未返回错误码与信息，且响应不是 200
     */
    RPC_UNKNOWN(100, "RPC error with none error code or msg."),
    /**
     * 请求错误：请求超时
     */
    REQUEST_TIMEOUT(101, "Request timeout."),
    /**
     * 请求错误：指定的请求方法不能被服务器处理
     */
    REQUEST_METHOD_MISMATCH(102, "The request method can't be processed by the server."),
    /**
     * 返回了错误码:xxx
     */
    RPC_COMMON(103, "RPC error with error code '%s'."),
    /**
     * 请求错误：实体格式不支持
     */
    REQUEST_BODY_INCORRECT(104, "Entity format not supported。"),

    // ----------------------- 作为服务提供者（要处理的HTTP请求参数校验未通过） ----------------------

    /**
     * 未知异常
     */
    UNKNOWN(300, "Unknown error."),
    /**
     * 响应超时
     */
    SERVICE_RESPONSE_TIMEOUT(301, "Service response timeout.", HttpStatus.REQUEST_TIMEOUT),
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(302, "Service unavailable.", Level.ERROR, HttpStatus.SERVICE_UNAVAILABLE),
    /**
     * 无效的安全会话 xSessionId 不正确 xSessionId in valid.
     */
    SECURITY_SESSION_INVALID(303, "Security session invalid.", Level.INFO, HttpStatus.UNAUTHORIZED),
    /**
     * 参数校验未通过，参数非法，通过第三方校验框架抛出
     */
    PARAM_NOT_VALID(304, "Parameter not valid. for %s", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 参数不能为空
     */
    PARAM_BLANK(305, "The required parameter %s is blank.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 参数范围不正确，如年龄传入了负数
     */
    PARAM_OUT_RANGE(306, "The value of parameter %s is not in the right range.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 参数格式不正确
     */
    PARAM_FORMAT_INVALID(307, "The format of parameter %s is not correct.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 参数错误：返回报文过长，可能未指定分页大小 or 分页过大
     */
    PARAM_PAGE_SETTING_INVALID(308, "Return message is too long, please resize page and retry.", HttpStatus.BAD_REQUEST),
    /**
     * 参数不支持
     */
    PARAM_NOT_SUPPORT(309, "The parameter(%s) not supported.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 参数内容长度超长，如用户名最大允许32个字符
     */
    PARAM_CONTENT_TOO_LONG(310, "The parameter(%s) content is out of limit.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 包装 HttpMessageNotReadableException,
     * 请求体读取失败，传过来的参数与你controller接收的参数类型不匹配。如 Post 请求缺少参数或者解析 json 时失败了
     */
    PARAM_BODY_NOT_READABLE(311, "HttpMessageNotReadable. %s", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 参数类型不匹配
     */
    PARAM_TYPE_NOT_MATCH(312, "MethodArgumentTypeMismatch. The value of %s(%s) resolved to %s fail.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * content-type 不正确
     */
    CONTENT_TYPE_INVALID(313, "HttpMediaTypeNotSupported. ContentType(%s) is not acceptable.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 文件上传出错
     */
    MULTIPART_INVALID(314, "Request is not a validate multipart request, please check request or file size.", HttpStatus.BAD_REQUEST),

    // ----------------------- 与中间件操作异常，代码正确时，常发于中间件宕机 ----------------------
    // 一般要包含连接什么异常、什么操作时失败 error 级别 返回 500
    MID_WARE_CONNECT_FAIL(400, "Connect ", Level.ERROR),
    PERSISTENCE_TO_DB_FAIL(401, "Persistent fail!", Level.ERROR),

    ;

    private String code;

    private String message;

    private Level logLevel;

    private HttpStatus httpStatus;

    CommonErrorCodeEnum(String code) {
        this.code = code;
    }

    CommonErrorCodeEnum(long code, String message) {
        this(code, message, DEFAULT_LOG_LEVEL, DEFAULT_HTTP_STATUS_CODE);
    }

    CommonErrorCodeEnum(long code, String message, HttpStatus httpStatus) {
        this(code, message, DEFAULT_LOG_LEVEL, httpStatus);
    }

    CommonErrorCodeEnum(long code, String message, Level logLevel) {
        this(code, message, logLevel, DEFAULT_HTTP_STATUS_CODE);
    }

    CommonErrorCodeEnum(long code, String message, Level logLevel, HttpStatus httpStatus) {
        this.code = Long.toHexString(code);
        this.message = message;
        this.logLevel = logLevel;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Level getLogLevel() {
        return logLevel;
    }

    @Override
    public HttpStatus getHttpStatusCode() {
        return httpStatus;
    }

    // 提供了两个生成异常的方法，可选择使用

    public BaseRuntimeException toException(Object... args) {
        return new BaseRuntimeException(this, args);
    }

    public BaseRuntimeException toException(Throwable t, Object... args) {
        return new BaseRuntimeException(this, t, args);
    }

}
