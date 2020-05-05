package org.shoulder.core.log;

import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 真正的 loggerFactory, 底层依赖的是 slf4j
 * @author lym
 */
public class ShoulderLoggerSl4jFactory implements ILoggerFactory {

    /**
     * 缓存
     */
    private static final ConcurrentMap<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    /**
     * 获取 logger
     *
     * @param key logger 标识
     * @return logger
     */
    @Override
    public Logger getLogger(Class<?> key) {
        ServiceLoader<Logger> loggerImpl = ServiceLoader.load(Logger.class);

        Logger logger = LOGGERS.get(key.getName());
        if (logger == null) {
            LOGGERS.putIfAbsent(key.getName(), new ShoulderLogger(key));
            logger = LOGGERS.get(key.getName());
        }
        return logger;
    }

    /**
     * 获取 logger
     *
     * @param key logger 标识
     * @return logger
     */
    @Override
    public Logger getLogger(String key) {
        Logger logger = LOGGERS.get(key);
        if (logger == null) {
            LOGGERS.putIfAbsent(key, new ShoulderLogger(key));
            logger = LOGGERS.get(key);
        }
        return logger;
    }

    public static ShoulderLoggerSl4jFactory getInstance(){
        return SingleTonHolder.instance;
    }

    private static class SingleTonHolder {
        static ShoulderLoggerSl4jFactory instance = new ShoulderLoggerSl4jFactory();
    }

}
