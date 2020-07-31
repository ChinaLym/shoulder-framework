package org.shoulder.core.log;

/**
 * 该接口为真正的 LoggerFactory
 * 一般来说，除非底层不是 slf4j, 使用者没必要关心该接口
 *
 * @author lym
 */
public interface ILoggerFactory {

    /**
     * 获取日志打印器
     *
     * @param loggerName 打印器名称
     * @return 日志打印器
     */
    Logger getLogger(String loggerName);

    /**
     * 获取日志打印器
     *
     * @param loggerName 打印器名称
     * @return 日志打印器
     */
    Logger getLogger(Class<?> loggerName);
}
