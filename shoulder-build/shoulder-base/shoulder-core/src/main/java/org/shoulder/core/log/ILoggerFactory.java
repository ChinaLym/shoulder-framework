package org.shoulder.core.log;

/**
 * 该接口为真正的 LoggerFactory
 *  一般来说，除非底层不是 slf4j, 使用者没必要关心该接口
 * @author lym
 */
public interface ILoggerFactory {

    Logger getLogger(String key);

    Logger getLogger(Class<?> key);
}
