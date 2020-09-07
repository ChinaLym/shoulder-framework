package org.shoulder.core.log;

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 真正的 loggerFactory, 底层依赖的是 slf4j
 *
 * @author lym
 */
public class ShoulderLoggerSl4jFactory implements ILoggerFactory {

    /**
     * 缓存
     */
    private static final ConcurrentMap<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    public static ShoulderLoggerSl4jFactory getInstance() {
        return SingleTonHolder.instance;
    }

    /**
     * 获取 logger
     *
     * @param loggerName logger 标识
     * @return logger
     */
    @Override
    public Logger getLogger(Class<?> loggerName) {
        ServiceLoader<Logger> loggerImpl = ServiceLoader.load(Logger.class);

        Logger logger = LOGGERS.get(loggerName.getName());
        if (logger == null) {
            LOGGERS.putIfAbsent(loggerName.getName(), new ShoulderLogger(loggerName));
            logger = LOGGERS.get(loggerName.getName());
        }
        return logger;
    }

    /**
     * 获取 logger
     *
     * @param loggerName logger 标识
     * @return logger
     */
    @Override
    public Logger getLogger(String loggerName) {
        Logger logger = LOGGERS.get(loggerName);
        if (logger == null) {
            LOGGERS.putIfAbsent(loggerName, new ShoulderLogger(loggerName));
            logger = LOGGERS.get(loggerName);
        }
        return logger;
    }

    private static class SingleTonHolder {
        static ShoulderLoggerSl4jFactory instance = new ShoulderLoggerSl4jFactory();
    }

}
