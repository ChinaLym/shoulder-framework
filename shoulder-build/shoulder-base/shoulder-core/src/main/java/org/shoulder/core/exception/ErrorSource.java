package org.shoulder.core.exception;

/**
 * @author lym
 */

public enum ErrorSource {

    /**
     * 调用者未按照约定调用：传入了错误的数据/越权访问/不在服务时间等
     */
    INVOKER,
    /**
     * 系统内部处理出错
     */
    SYSTEM,
    /**
     * 系统依赖的第三方能力失败，导致处理失败
     */
    THIRD,
    ;

}
