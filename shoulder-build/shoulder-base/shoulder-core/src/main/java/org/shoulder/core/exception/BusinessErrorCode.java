package org.shoulder.core.exception;

import jakarta.annotation.Nonnull;
import org.shoulder.core.context.AppInfo;

/**
 * 错误码接口【业务代码使用】
 *
 * @author lym
 */
public interface BusinessErrorCode extends ErrorCode {

    // --------------------------- 业务代码实现使用 ----------------------------

    /**
     * 模块标识码
     *
     * @return 模块标识码
     */
    default String moduleCode() {
        //return "000";
        throw new UnsupportedOperationException(getClass() + " not implement the method!");
    }

    /**
     * 具体的错误号
     *
     * @return 具体的错误号
     */
    default String specialErrorCode() {
        throw new UnsupportedOperationException(getClass() + " not implement the method!");
    }


    // --------------------------- 框架使用 ----------------------------

    /**
     * 完整的错误码
     *
     * @return 错误码
     */
    @Nonnull
    @Override
    default String getCode() {
        return getErrorCodePrefix() + moduleCode() + specialErrorCode();
    }

    /**
     * 错误码前缀（每个组件唯一，用于区分哪个组件）
     *
     * @return 本组件的错误码前缀
     */
    default String getErrorCodePrefix() {
        return AppInfo.errorCodePrefix();
    }

}
