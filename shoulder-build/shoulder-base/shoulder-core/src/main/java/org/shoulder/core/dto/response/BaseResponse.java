package org.shoulder.core.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.exception.ErrorCode;
import org.shoulder.core.util.ExceptionUtil;

import java.io.Serializable;

/**
 * 通用返回对象
 *
 * 统一返回值，code，msg，data
 *
 * @author lym
 */
@ApiModel(value = "通用 HTTP 响应 DTO 格式")
//@Schema(name = "通用 HTTP 响应 DTO")
public class BaseResponse<T> implements Serializable {

    @ApiModelProperty(value = "通用 HTTP 响应 DTO", dataType = "String", required = true, example = "0", position = 0)
    //@Schema(name = "状态码，成功为0，失败非0", example = "0")
    private String code = "0";

    @ApiModelProperty(value = "描述，一般成功时不需要该值", dataType = "String", required = false, example = "success", position = 1)
    //@Schema(name = "描述，一般成功时不需要该值", example = "success")
    private String msg = "success";

    @ApiModelProperty(value = "传输的数据", dataType = "Object", required = false, example = "{\"name\":\"shoulder\"}", position = 2)
    //@Schema(name = "传输的数据")
    private T data;

    public BaseResponse() {
    }

    /**
     * 构造器
     * @param errorCode 错误码
     */
    public BaseResponse(ErrorCode errorCode) {
        setCode(errorCode.getCode());
        setMsg(errorCode.getMessage());
    }


    /**
     * 构造器
     * @param code 错误码
     * @param msg  提示信息
     * @param data 返回数据
     */
    public BaseResponse(String code, String msg, T data) {
        setCode(code);
        setMsg(msg);
        setData(data);
    }


    public static <T> BaseResponse<T> success() {
        return new BaseResponse<T>(ErrorCode.SUCCESS);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<T>(ErrorCode.SUCCESS).setData(data);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<T>(errorCode);
    }

    public static <T> BaseResponse<T> error(ErrorCode error, T data) {
        return new BaseResponse<T>(error).setData(data);
    }


    /**
     * 获取 data 用于获取 api 返回值时使用，当 code 不为 success 时抛出传入的异常类型
     */
    @JsonIgnore
    public T getOrException() {
        // success
        if (ErrorCode.SUCCESS.getCode().equals(code)) {
            return data;
        }
        throw CommonErrorCodeEnum.RPC_COMMON.toException(code);
    }

    /**
     * 检查 code，若不为 SUCCESS，则抛异常
     */
    public void checkCode() {
        if (!ErrorCode.SUCCESS.getCode().equals(code)) {
            throw new BaseRuntimeException(code, msg);
        }
    }


    public String getCode() {
        return code;
    }

    public BaseResponse<T> setCode(String code) {
        this.code = ExceptionUtil.formatErrorCode(code);
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
