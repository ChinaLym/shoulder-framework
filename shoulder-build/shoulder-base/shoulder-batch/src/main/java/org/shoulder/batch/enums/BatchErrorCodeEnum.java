package org.shoulder.batch.enums;

import org.shoulder.core.exception.ErrorCode;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

import javax.annotation.Nonnull;

/**
 * 批量相关错误码
 *
 * @author lym
 */
public enum BatchErrorCodeEnum implements ErrorCode {

    /**
     * taskId 不存在，接口调用错误
     */
    TASK_ID_NOT_EXIST(0, "The task id not exist!"),

    /**
     * 当前任务过多，拒绝处理
     */
    IMPORT_BUSY(0, "Import handler is busy, please retry later!"),

    /**
     * 不支持该导出方式
     */
    EXPORT_TYPE_NOT_SUPPORT(0, "Not support export with such type: %s"),

    /**
     * 不支持该导出方式
     */
    DATA_TYPE_OR_OPERATION_NOT_SUPPORT(0, "Not support such dataType(%s) or operation(%s)"),

    /**
     * 分片任务结果数不正确，可能未全部执行完便异常终止了
     */
    TASK_SLICE_RESULT_INVALID(0, "task slice result invalid, except %d but only %d"),

    ;

    private String code;

    private String message;

    private Level logLevel;

    private HttpStatus httpStatus;

    BatchErrorCodeEnum(long code, String message) {
        this(code, message, Level.WARN, HttpStatus.BAD_REQUEST);
    }

    BatchErrorCodeEnum(long code, String message, HttpStatus httpStatus) {
        this(code, message, DEFAULT_LOG_LEVEL, httpStatus);
    }

    BatchErrorCodeEnum(long code, String message, Level logLevel) {
        this(code, message, logLevel, HttpStatus.BAD_REQUEST);
    }

    BatchErrorCodeEnum(long code, String message, Level logLevel, HttpStatus httpStatus) {
        this.code = Long.toHexString(code);
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
