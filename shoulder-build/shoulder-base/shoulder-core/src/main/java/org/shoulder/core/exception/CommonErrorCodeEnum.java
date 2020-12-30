package org.shoulder.core.exception;

import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;


/**
 * 通用错误（2^14以下框架使用）错误码标识 = 0 的那部分
 *
 * @author lym
 */
public enum CommonErrorCodeEnum implements ErrorCode {

    // ------------------------------- 认证 -----------------------------
    /**
     * @desc 未认证，需要认证后才能访问
     * @sug 先进行认证，再访问服务
     */
    AUTH_401_NEED_AUTH(1, "Need Authentication.", Level.INFO, HttpStatus.UNAUTHORIZED),
    /**
     * @desc 认证失败：如用户名或密码凭据无效、由于白名单等被拒绝、未开启匿名访问等
     * @sug 检查认证凭证是否正确且在有效期内
     */
    AUTH_401_UNAUTHORIZED(2, "Authentication failed.", Level.INFO, HttpStatus.UNAUTHORIZED),
    /**
     * @desc 认证过期，需要重新认证
     * @sug
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
    FILE_CREATE_FAIL(100, "Failed to create the file.", Level.ERROR),
    /**
     * 文件系统错误：读文件失败
     */
    FILE_READ_FAIL(101, "Failed to write the file.", Level.ERROR),
    /**
     * 文件系统错误：写文件失败
     */
    FILE_WRITE_FAIL(102, "Failed to write the file.", Level.ERROR),
    /**
     * 文件系统错误：删除文件失败
     */
    FILE_DELETE_FAIL(103, "Failed to delete the file.", Level.ERROR),

    // ------------- 发起网络请求时（作为服务消费者）该类错误一般不会直接抛出去，通常会再次捕获，包装后抛出业务异常 -------------

    /**
     * 未知异常，对方未遵循标准格式，未返回错误码与信息，且响应不是 200
     */
    RPC_UNKNOWN(200, "RPC error with none error code or msg.", Level.ERROR),
    /**
     * 请求错误：请求超时
     */
    REQUEST_TIMEOUT(201, "Request timeout."),
    /**
     * 请求错误：指定的请求方法不能被服务器处理
     */
    REQUEST_METHOD_MISMATCH(202, "The request method can't be processed by the server.", Level.ERROR),
    /**
     * 调用 xxx 返回了错误码:xxx
     */
    RPC_COMMON(203, "Invoke %s fail with error code '%s'."),
    /**
     * 请求错误：实体格式不支持
     */
    REQUEST_BODY_INCORRECT(204, "Entity format not supported。", Level.ERROR),

    // ----------------------- 作为服务提供者（要处理的HTTP请求参数校验未通过） ----------------------

    /**
     * 未知异常，谨慎使用该错误码，不利于排查
     */
    UNKNOWN(300, "Unknown error."),
    /**
     * 响应超时（对于网关）
     */
    SERVICE_RESPONSE_TIMEOUT(301, "Service response timeout.", HttpStatus.REQUEST_TIMEOUT),
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(302, "Service unavailable.", Level.ERROR, HttpStatus.SERVICE_UNAVAILABLE),
    /**
     * 不再支持的接口（接口已废弃）
     */
    DEPRECATED_NOT_SUPPORT(305, "Function not support any more.", Level.ERROR, HttpStatus.BAD_REQUEST),

    /**
     * 包装 HttpMessageNotReadableException,
     * 请求体读取失败，传过来的参数与你controller接收的参数类型不匹配。如 Post 请求缺少参数或者解析 json 时失败了
     */
    PARAM_BODY_NOT_READABLE(321, "HttpMessageNotReadable. %s", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * content-type 不正确
     */
    CONTENT_TYPE_INVALID(323, "HttpMediaTypeNotSupported. ContentType(%s) is not acceptable.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 文件上传出错
     */
    MULTIPART_INVALID(324, "Request is not a validate multipart request, please check request or file size.", HttpStatus.BAD_REQUEST),

    // ----------------------- 并发、达到瓶颈 error 级别 返回 500 ----------------------

    SERVER_BUSY(399, "server is busy, try again later.", Level.ERROR),


    // ----------------------- 与中间件操作异常，代码正确时，常发于中间件宕机 ----------------------


    // 一般要包含连接什么异常、什么操作时失败 error 级别 返回 500
    MID_WARE_CONNECT_FAIL(400, "Connect ", Level.ERROR),

    PERSISTENCE_TO_DB_FAIL(401, "Persistent fail!", Level.ERROR),

    ;

    private String code;

    private String message;

    private Level logLevel;

    private HttpStatus httpStatus;

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
        String hex = Long.toHexString(code);
        this.code = "0x" + "0".repeat(Math.max(0, 8 - hex.length())) + hex;
        this.message = message;
        this.logLevel = logLevel;
        this.httpStatus = httpStatus;
    }

    @Nonnull
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

}
