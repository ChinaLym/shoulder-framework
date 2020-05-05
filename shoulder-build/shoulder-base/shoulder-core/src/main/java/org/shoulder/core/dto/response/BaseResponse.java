package org.shoulder.core.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.IError;
import org.shoulder.core.constant.CommonErrorEnum;

import java.io.Serializable;
import java.lang.reflect.Constructor;

/**
 * 通用返回对象
 *
 * @author lym
 */
@Schema(name = "通用 HTTP 响应 DTO")
public class BaseResponse<T> implements Serializable {

    @Schema(name = "状态码，成功为0，失败非0", example = "0")
    private String code = "0";

    @Schema(name = "描述，一般成功时不需要该值", example = "success")
    private String msg = "success";

    @Schema(name = "传输的数据")
    private T data;

    public BaseResponse() {
    }


    public BaseResponse(IError error) {
        this.code = error.getCode();
        this.msg = error.getMessage();
    }

    public BaseResponse(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public static <T> BaseResponse<T> success() {
        return new BaseResponse<T>(CommonErrorEnum.SUCCESS);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<T>(CommonErrorEnum.SUCCESS).setData(data);
    }

    public static <T> BaseResponse<T> error(IError errorCode) {
        return new BaseResponse<T>(errorCode);
    }

    public static <T> BaseResponse<T> error(IError error, T data) {
        return new BaseResponse<T>(error).setData(data);
    }


    // 用于获取 api 返回值时使用

    @JsonIgnore
    public T getOrException() {
        return getOrException(BaseRuntimeException.class);
    }

    /**
     * 获取 data
     * 当 code 不为 success 时抛出传入的异常类型
     */
    @JsonIgnore
    public T getOrException(Class<? extends BaseRuntimeException> exceptionType) {
        return getOrException(exceptionType, null);
    }

    /**
     * @param exceptionType 抛什么异常
     * @param customMessage 自定义的异常描述信息
     */
    @JsonIgnore
    public T getOrException(Class<? extends BaseRuntimeException> exceptionType, String customMessage) {
        // success
        if (CommonErrorEnum.SUCCESS.getCode().equals(code)) {
            return data;
        }
        String actualMessage = customMessage != null ? customMessage : msg;

        // BaseRuntimeException
        if (BaseRuntimeException.class == exceptionType) {
            throw new BaseRuntimeException(code, actualMessage);
        }

        // customer exception
        try {
            Constructor<? extends BaseRuntimeException> constructor = exceptionType.getConstructor(String.class,
                    String.class);
            throw constructor.newInstance(code, actualMessage);
        } catch (Exception e) {
            throw new BaseRuntimeException(code, actualMessage, e);
        }
    }

    /**
     * 检查 code，若不为 SUCCESS，则抛异常
     */
    public void checkCode() {
        if (!CommonErrorEnum.SUCCESS.getCode().equals(code)) {
            throw new BaseRuntimeException(code, msg);
        }
    }


    public String getCode() {
        return code;
    }

    public BaseResponse<T> setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public BaseResponse<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public BaseResponse<T> setData(T data) {
        this.data = data;
        return this;
    }

}
