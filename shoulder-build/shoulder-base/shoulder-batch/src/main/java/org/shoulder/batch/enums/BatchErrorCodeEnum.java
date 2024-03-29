package org.shoulder.batch.enums;

import jakarta.annotation.Nonnull;
import org.shoulder.core.exception.ErrorCode;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

/**
 * 批量相关错误码
 *
 * @author lym
 */
public enum BatchErrorCodeEnum implements ErrorCode {

    /**
     * 该id批处理任务不存在，接口调用错误
     */
    BATCH_ID_NOT_EXIST(0, "BatchId not exist!"),

    /**
     * 任务状态不合适，通常是任务上一个节点还没完成，数据还没准备好，校验未完成，已经完成了，但不允许导入，需要查询任务进度来确认为什么不能执行
     */
    TASK_STATUS_ERROR(0, "Illegal task status!"),

    /**
     * 当前任务过多，拒绝处理
     */
    IMPORT_BUSY(0, "Import handler is busy, please retry later!"),

    /**
     * 导入的 csv Header 不正确
     */
    CSV_HEADER_ERROR(0, "Csv header format error, check header please. Keep format like importTemplate please!"),

    /**
     * 不支持该导出方式
     */
    EXPORT_TYPE_NOT_SUPPORT(0, "Not support export with such type: %s"),

    /**
     * 不支持该导出方式
     */
    DATA_TYPE_OR_OPERATION_NOT_SUPPORT(0, "Not support such dataType(%s) or operation(%s), please check param."),

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
        this(code, message, Level.WARN, httpStatus);
    }

    BatchErrorCodeEnum(long code, String message, Level logLevel) {
        this(code, message, logLevel, HttpStatus.BAD_REQUEST);
    }

    BatchErrorCodeEnum(long code, String message, Level logLevel, HttpStatus httpStatus) {
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
