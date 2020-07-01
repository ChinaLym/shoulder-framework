package org.shoulder.core.exception;

import org.shoulder.core.dto.response.BaseResponse;
import org.shoulder.core.util.ExceptionUtil;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotEmpty;

/**
 * 表示错误的接口
 * 必带错误码，可带提示信息的异常/错误
 * 通常错误码枚举、自定义异常类实现该接口
 *
 * 小提示：业务中可以按模块或按业务定义枚举 保存错误码、错误提示信息
 *
 * @author lym
 */
public interface ErrorCode {
    /**
     * 获取错误码
     * @return 错误码
     */
    @NotEmpty
    String getCode();

    /**
     * 获取错误信息
     * @return 错误信息，支持 %s、{} 这种带占位符的消息
     */
    String getMessage();

    /**
     * 获取用于填充错误信息的数据
     * @return 用于填充错误信息的数据
     */
    default Object[] getArgs(){
        return null;
    }

    /**
     * 设置用于填充错误信息的数据
     * @param args 用于填充错误信息的数据
     */
    default void setArgs(Object... args){
        // todo 是否抛出异常？
        throw new UnsupportedOperationException("not support set args");
    }

    /**
     * 转化为 api 返回值
     *     // todo 返回值data必须为数组？
     * @param args 填充异常信息的参数
     * @return api 返回值
     */
    default BaseResponse<Object[]> toResponse(Object... args){
        return new BaseResponse<>(
                this.getCode(),
                this.getMessage(),
                args == null ? getArgs() : args
        );
    }

    /**
     * 生成详情
     * 仅在作为接口返回值、记录本地日志信息时使用
     * @return 填充参数后的 msg
     */
    default String generateDetail(){
        return ExceptionUtil.generateExceptionMessage(getMessage(), getArgs());
    }

    /**
     * 抛出运行异常
     * @throws BaseRuntimeException e
     */
    default void throwRuntime() throws BaseRuntimeException {
        throw new BaseRuntimeException(this);
    }

    /**
     * 包装 t 并抛出运行异常
     * @param t 异常
     * @throws BaseRuntimeException e
     */
    default void throwRuntime(Throwable t) throws BaseRuntimeException {
        throw new BaseRuntimeException(getCode(), getMessage(), t);
    }

}
