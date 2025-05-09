package org.shoulder.validate.exception;

import jakarta.annotation.Nonnull;
import org.shoulder.core.exception.ErrorCode;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;


/**
 * 通用参数错误码枚举
 * 默认记录 info 日志，返回 400 httpCode
 * 使用统一的参数非法代替
 *
 * @author lym
 */
public enum ParamErrorCodeEnum implements ErrorCode {


    // ----------------------- 作为服务提供者（要处理的HTTP请求参数校验未通过） ----------------------

    /**
     * 参数校验未通过，参数非法（非业务代码 或 内部约定格式，非公开隐蔽接口，一般不使用）
     */
    PARAM_ILLEGAL(314, "Parameter illegal. for '%s'."),
    /**
     * 参数为空
     */
    PARAM_BLANK(315, "The required parameter '%s' is blank."),
    /**
     * 参数范围不正确，如年龄传入了负数
     */
    PARAM_OUT_RANGE(316, "The value of parameter '%s' is not in the right range."),
    /**
     * 参数格式不正确（正则）
     */
    PARAM_FORMAT_INVALID(317, "The format of parameter '%s' is not correct."),
    /**
     * 参数错误：返回报文过长，可能未指定分页大小 or 分页过大
     */
    PARAM_PAGE_SETTING_INVALID(318, "Return data is too big, please resize page and retry."),
    /**
     * 参数不支持，一般不检查，除非明确接口中不得传入额外参数
     */
    PARAM_NOT_SUPPORT(319, "The parameter '%s' is not supported."),
    /**
     * 参数内容长度超长，如用户名最大允许32个字符
     */
    PARAM_CONTENT_TOO_LONG(310, "The parameter '%s' content is out of limit."),
    /**
     * 参数类型不匹配，如希望 int 1 ，传入的为 string "1"
     * Fail to resolve param 'abc'(String)  to expect type(Integer).
     */
    PARAM_TYPE_NOT_MATCH(322, "MethodArgumentTypeMismatch. Fail to resolve param '%s'(%s)  to expect type(%s)."),
    /**
     * 数据已存在
     */
    DATA_REPEAT(322, "The data already exist, reject for repeat."),
    /**
     * 数据不存在
     */
    DATA_NON_EXIST(322, "Data not exist: %s."),

    ;

    private String code;

    private String message;

    private Level logLevel;

    private HttpStatus httpStatus;

    ParamErrorCodeEnum(long code, String message) {
        this(code, message, Level.WARN, HttpStatus.BAD_REQUEST);
    }

    ParamErrorCodeEnum(long code, String message, HttpStatus httpStatus) {
        this(code, message, Level.WARN, httpStatus);
    }

    ParamErrorCodeEnum(long code, String message, Level logLevel) {
        this(code, message, logLevel, HttpStatus.BAD_REQUEST);
    }

    ParamErrorCodeEnum(long code, String message, Level logLevel, HttpStatus httpStatus) {
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
