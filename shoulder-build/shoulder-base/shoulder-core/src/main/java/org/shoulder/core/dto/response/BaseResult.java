package org.shoulder.core.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import org.shoulder.core.dto.ToStringObj;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.exception.ErrorCode;
import org.shoulder.core.exception.ErrorContext;
import org.shoulder.core.util.ExceptionUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Restful 风格返回值
 * <p>
 * 统一接口返回值格式，包含 code，msg，data，错误时包含 errorContext
 * 接口版本不兼容变更，需要约定返回版本号的位置，通常在响应头，以便于接收者感知处理
 *
 * @author lym
 */
@Schema(description = "BaseResult<T> 接口响应统一返回值包装类 Restful 风格")
public class BaseResult<T> extends ToStringObj {

    private static final long serialVersionUID = -3829563105110651627L;

    @Schema(description = "状态码/错误码，成功为0，失败非0，必定返回", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    protected String code = "0";

    @Schema(description = "响应描述，成功时一般不需要该值，必定返回", example = "success")
    protected String msg = "success";

    @Schema(description = "传输的数据", type = "Object", example = "{\"name\":\"shoulder\"}")
    protected T data;

    /**
     * 预留的扩展属性
     */
    @Schema(description = "扩展属性", type = "", example = "")
    private Map<String, Object> ext = Collections.emptyMap();

    public BaseResult() {
    }

    /**
     * 构造器
     *
     * @param errorCode 错误码
     */
    public BaseResult(ErrorCode errorCode) {
        setCode(errorCode.getCode());
        setMsg(errorCode.getMessage());
    }

    /**
     * 构造器
     *
     * @param code 错误码
     * @param msg  提示信息
     * @param data 返回数据
     */
    public BaseResult(String code, String msg, T data) {
        setCode(code);
        setMsg(msg);
        setData(data);
    }


    public static <T> BaseResult<T> success() {
        return new BaseResult<>(ErrorCode.SUCCESS);
    }

    public static <T> BaseResult<T> success(T data) {
        return new BaseResult<T>(ErrorCode.SUCCESS).setData(data);
    }

    public static <X> BaseResult<ListResult<X>> success(Collection<? extends X> dataList) {
        ListResult<X> listData = ListResult.of(dataList);
        return new BaseResult<ListResult<X>>(ErrorCode.SUCCESS).setData(listData);
    }

    public static BaseResult<Void> error(ErrorCode errorCode) {
        return new BaseResult<>(errorCode);
    }

    public static BaseResult<Void> error(ErrorCode error, String msg) {
        return new BaseResult<Void>(error).setMsg(msg);
    }


    /**
     * 获取 data 用于获取 api 返回值时使用，当 code 不为 success 时抛出传入的异常类型
     */
    @JsonIgnore
    public T getOrException() {
        // success
        if (isSuccess()) {
            return data;
        }
        throw new BaseRuntimeException(CommonErrorCodeEnum.RPC_FAIL_WITH_CODE, code);
    }

    /**
     * 检查 code，若不为 SUCCESS，则抛异常
     */
    public void checkCode() {
        if (!isSuccess()) {
            throw new BaseRuntimeException(code, msg);
        }
    }

    @JsonIgnore
    public boolean isSuccess() {
        return ErrorCode.SUCCESS_CODE.equals(code);
    }


    public String getCode() {
        return code;
    }

    public BaseResult<T> setCode(String code) {
        this.code = ExceptionUtil.formatErrorCode(code);
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public BaseResult<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public BaseResult<T> setData(T data) {
        this.data = data;
        return this;
    }

    @JsonIgnore
    public ErrorContext getErrorContext() {
        return getExt("errorContext");
    }

    @JsonIgnore
    public void setErrorContext(ErrorContext errorContext) {
        setExt("errorContext", errorContext);
    }

    @JsonIgnore
    public BaseResult<T> setExt(String key, Object value) {
        if (this.ext == Collections.EMPTY_MAP) {
            // 一般扩展属性不会太多，默认4
            this.ext = new HashMap<>();
        }
        ext.put(key, value);
        return this;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public <ANY> ANY getExt(String key) {
        return (ANY) ext.remove(key);
    }

    public BaseResult<T> removeExt(String key) {
        ext.remove(key);
        return this;
    }

}
