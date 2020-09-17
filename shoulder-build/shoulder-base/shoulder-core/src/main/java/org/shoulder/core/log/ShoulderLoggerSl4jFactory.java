package org.shoulder.core.log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 真正的 loggerFactory, 底层依赖的是 slf4j
 *
 * @author lym
 */
public class ShoulderLoggerSl4jFactory implements ILoggerFactory {

    /**
     * Logger 缓存。不同项目，必然有不同初始值，势必引起多次扩容，但由于最终会达到一个稳定状态，故可容忍
     */
    private static final ConcurrentMap<String, Logger> LOGGER_CACHE = new ConcurrentHashMap<>(256);

    public static ShoulderLoggerSl4jFactory getInstance() {
        return SingleTonHolder.instance;
    }

    private ShoulderLoggerSl4jFactory(){}

    /**
     * 获取 logger
     *
     * @param loggerClazz logger 标识
     * @return logger
     */
    @Override
    public Logger getLogger(Class<?> loggerClazz) {
        return getLogger(loggerClazz.getName());
    }

    /**
     * 获取 logger
     *
     * @param loggerName logger 标识
     * @return logger
     */
    @Override
    public Logger getLogger(String loggerName) {
        return LOGGER_CACHE.computeIfAbsent(loggerName, ShoulderLogger::new);
    }

    private static class SingleTonHolder {
        static ShoulderLoggerSl4jFactory instance = new ShoulderLoggerSl4jFactory();
    }

}
