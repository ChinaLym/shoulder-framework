package org.shoulder.core.exception;

import jakarta.annotation.Nonnull;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * 通用错误（2^14以下框架使用）错误码标识 = 0 的那部分
 * 多数通用错误，展示时仅为未知错误，dev环境可详情，堆栈，错误码映射表地址
 *
 * @author lym
 */
public enum CommonErrorCodeEnum implements ErrorCode {

    // ------------------------------- 认证 -----------------------------
    /**
     * @desc 未认证，需要认证后才能访问
     * @sug 先进行认证，再访问服务
     */
    AUTH_401_NEED_AUTH(11, "Need Authentication.", Level.INFO, HttpStatus.UNAUTHORIZED),
    /**
     * @desc 认证失败：如用户名或密码凭据无效、由于白名单等被拒绝、未开启匿名访问等
     * @sug 检查认证凭证是否正确且在有效期内
     */
    AUTH_401_UNAUTHORIZED(12, "Authentication failed.", Level.INFO, HttpStatus.UNAUTHORIZED),
    /**
     * @desc 认证无效，需要先进行认证
     * @sug 1.未携带正确认证标识（需要检查接口文档-认证）；2.过期的认证标识（需要重新登陆）
     * @see org.springframework.security.web.access.ExceptionTranslationFilter#handleSpringSecurityException(jakarta.servlet.http.HttpServletRequest, jakarta.servlet.http.HttpServletResponse, jakarta.servlet.FilterChain, java.lang.RuntimeException) AccessDeny
     */
    AUTH_401_EXPIRED(13, "Certification invalid. Do-auth please.", Level.INFO, HttpStatus.UNAUTHORIZED),

    /**
     * 主动拒绝请求：权限不够
     */
    PERMISSION_DENY(18, "Permission deny.", Level.INFO, HttpStatus.FORBIDDEN),
    /**
     * 主动拒绝请求：令牌无效
     */
    AUTH_403_TOKEN_INVALID(19, "Invalid token.", Level.INFO, HttpStatus.FORBIDDEN),
    /**
     * 租户无效：不存在 / 封禁 / 冻结
     */
    TENANT_INVALID(40, "Invalid tenant.", Level.INFO, HttpStatus.FORBIDDEN),
    /**
     * 操作非法：租户信息与业务不匹配 / 数据路由错误
     */
    ILLEGAL_OPERATION(50, "illegal operation.", Level.INFO, HttpStatus.FORBIDDEN),


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
    RPC_TIMEOUT(201, "Request timeout.", Level.WARN),
    /**
     * 请求错误：指定的请求方法不能被服务器处理
     */
    REQUEST_METHOD_MISMATCH(202, "The request method('%s') can't be processed by the server. Only support [%s].", Level.WARN),
    /**
     * 调用 xxx 返回了错误码:xxx
     */
    RPC_FAIL_WITH_CODE(203, "Invoke %s fail with error code '%s'.", Level.ERROR),
    /**
     * 请求错误：实体格式不支持
     */
    REQUEST_BODY_INCORRECT(204, "Entity format not supported.", Level.ERROR),

    ILLEGAL_PARAM(205, "illegal param.", Level.ERROR),

    // ----------------------- 作为服务提供者（要处理的HTTP请求参数校验未通过） ----------------------

    /**
     * 未知异常，谨慎使用该错误码，不利于排查；一般只用于断言正常一定怎样，如根据索引更新，更新影响数目一定小于等于1;或者编码时使用者未按照设计者的思路使用
     */
    UNKNOWN(300, "UNKNOWN ERROR.", Level.ERROR, HttpStatus.BAD_REQUEST),
    /**
     * 未知异常，谨慎使用该错误码，不利于排查；一般只用于断言正常一定怎样，如根据索引更新，更新影响数目一定小于等于1;或者编码时使用者未按照设计者的思路使用
     */
    PROCESS_FAIL(301, "PROCESS FAIL.", Level.ERROR, HttpStatus.BAD_REQUEST),
    /**
     * 编码错误，仅在框架 / 工具内使用，提示使用者使用错误
     */
    CODING(305, "Coding error.", Level.ERROR, HttpStatus.BAD_REQUEST),
    /**
     * 重复提交：参数完全相同 / 检测到幂等且不支持幂等
     */
    REPEATED_SUBMIT(311, "Repeated submit"),
    /**
     * 非当且分片数据：系统有逻辑数据分片，但路由等问题导致错误的服务器收到了不属于服务器处理的分片数据
     *
     * @deprecated use unknown
     */
    SLICE_NOT_MATCH(312, "Slice not match"),
    /**
     * 响应超时（对于网关）
     */
    SERVICE_RESPONSE_TIMEOUT(321, "Service response timeout.", Level.ERROR, HttpStatus.REQUEST_TIMEOUT),
    /**
     * 服务不可用（已经降级）
     */
    SERVICE_UNAVAILABLE(322, "Service unavailable.", Level.ERROR, HttpStatus.SERVICE_UNAVAILABLE),
    /**
     * 不再支持的接口（接口已废弃）
     */
    DEPRECATED_NOT_SUPPORT(325, "Function not support any more.", Level.ERROR, HttpStatus.BAD_REQUEST),

    /**
     * 包装 HttpMessageNotReadableException；请求体读取失败：传来的参数与controller声明的参数类型不匹配。如 Post 请求缺少参数或者解析 json 时失败了
     */
    PARAM_BODY_NOT_READABLE(331, "HttpMessageNotReadable. %s", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * content-type 不正确
     */
    CONTENT_TYPE_INVALID(333, "HttpMediaTypeNotSupported. ContentType(%s) is not acceptable.", Level.INFO, HttpStatus.BAD_REQUEST),
    /**
     * 文件上传出错
     */
    MULTIPART_INVALID(334, "Request is not a validate multipart request, please check request or file size.", Level.WARN, HttpStatus.BAD_REQUEST),

    /**
     * 状态检查未通过
     */
    ILLEGAL_STATUS(340, "illegal status.", Level.ERROR),

    /**
     * 数据版本过旧
     */
    DATA_VERSION_EXPIRED(350, "数据版本过旧，可能已经被其他人修改"),

    /**
     * 数据不存在
     */
    DATA_NOT_EXISTS(351, "数据不存在"),

    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(352, "数据已存在"),

    // ----------------------- 并发、达到瓶颈 error 级别 返回 500 ----------------------

    /**
     * @desc 服务器繁忙
     * @sug 请稍后再试
     */
    SERVER_BUSY(399, "server is busy, try again later.", Level.ERROR),


    // ----------------------- 与中间件操作异常，代码正确时，常发于中间件宕机 ----------------------

    /**
     * 连接xxx中间件异常（配置信息有误/中间件宕机）、xxx操作时失败通常 error 级别 返回 500
     */
    MID_WARE_CONNECT_FAIL(400, "Connect ", Level.ERROR),
    /**
     * 数据存储失败-未落库
     */
    DATA_STORAGE_FAIL(401, "Persistent fail! %s", Level.ERROR),
    /**
     * 数据访问错误-除了保存时
     */
    DATA_ACCESS_FAIL(402, "Data access fail!", Level.ERROR),

    ;

    private String code;

    private String message;

    private Level logLevel;

    private HttpStatus httpStatus;

    CommonErrorCodeEnum(long code, String message) {
        this(code, message, Level.ERROR, DEFAULT_HTTP_STATUS_CODE);
    }

    CommonErrorCodeEnum(long code, String message, HttpStatus httpStatus) {
        this(code, message, Level.ERROR, httpStatus);
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

    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        CommonErrorCodeEnum[] codeEnums = CommonErrorCodeEnum.class.getEnumConstants();
        for (CommonErrorCodeEnum codeEnum : codeEnums) {
            Method method = CommonErrorCodeEnum.class.getMethod("getCode");
            String code = (String) method.invoke(codeEnum);
            System.out.println(code);
        }
    }

}
