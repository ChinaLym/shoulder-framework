package org.shoulder.core.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.exception.ErrorCode;
import org.shoulder.core.util.ExceptionUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Restful 风格返回值
 * <p>
 * 统一接口返回值格式，包含 code，msg，data
 * 接口版本不兼容变更，需要约定返回版本号的位置，通常在响应头，以便于接收者感知处理
 *
 * @author lym
 */
@ApiModel(value = "接口响应统一返回值包装类 Restful 风格")
//@Schema(name = "接口响应统一返回值包装类 Restful 风格")
public class RestResult<T> implements Serializable {

    @ApiModelProperty(value = "状态码/错误码，成功为0，失败非0，必定返回", dataType = "String", required = true, example = "0", position = 0)
    //@Schema(name = "状态码/错误码，成功为0，失败非0", example = "0")
    private String code = "0";

    @ApiModelProperty(value = "响应描述，成功时一般不需要该值，必定返回", dataType = "String", required = false, example = "success", position = 1)
    //@Schema(name = "响应描述，成功时一般不需要该值", example = "success")
    private String msg = "success";

    @ApiModelProperty(value = "传输的数据", dataType = "Object", required = false, example = "{\"name\":\"shoulder\"}", position = 2)
    //@Schema(name = "传输的数据")
    private T data;

    /**
     * 预留的扩展属性
     */
    private Map<String, Object> ext = Collections.emptyMap();

    public RestResult() {
    }

    /**
     * 构造器
     *
     * @param errorCode 错误码
     */
    public RestResult(ErrorCode errorCode) {
        setCode(errorCode.getCode());
        setMsg(errorCode.getMessage());
    }

    public RestResult<T> addExt(String key, Object value) {
        if (this.ext == Collections.EMPTY_MAP) {
            // 一般扩展属性不会太多，默认4
            this.ext = new HashMap<>(4);
        }
        ext.put(key, value);
        return this;
    }

    /**
     * 构造器
     *
     * @param code 错误码
     * @param msg  提示信息
     * @param data 返回数据
     */
    public RestResult(String code, String msg, T data) {
        setCode(code);
        setMsg(msg);
        setData(data);
    }


    public static <T> RestResult<T> success() {
        return new RestResult<T>(ErrorCode.SUCCESS);
    }

    public static <T> RestResult<T> success(T data) {
        return new RestResult<T>(ErrorCode.SUCCESS).setData(data);
    }

    public static <X> RestResult<ListResult<X>> success(Collection<? extends X> dataList) {
        ListResult<X> listData = ListResult.of(dataList);
        return new RestResult<ListResult<X>>(ErrorCode.SUCCESS).setData(listData);
    }

    public static <T> RestResult<T> error(ErrorCode errorCode) {
        return new RestResult<T>(errorCode);
    }

    public static <T> RestResult<T> error(ErrorCode error, T data) {
        return new RestResult<T>(error).setData(data);
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
        throw new BaseRuntimeException(CommonErrorCodeEnum.RPC_COMMON, code);
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

    public RestResult<T> setCode(String code) {
        this.code = ExceptionUtil.formatErrorCode(code);
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public RestResult<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public RestResult<T> setData(T data) {
        this.data = data;
        return this;
    }

}
