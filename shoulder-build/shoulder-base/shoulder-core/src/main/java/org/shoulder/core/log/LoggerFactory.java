package org.shoulder.core.log;

import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Shoulder loggerFactory Facade 门面，用于加载 ILoggerFactory
 *
 * @author lym
 */
public class LoggerFactory {

    /**
     * 真正的 loggerFactory
     */
    private static final ILoggerFactory DELEGATE;

    // 绑定日志工厂
    static {
        ServiceLoader<ILoggerFactory> loads = ServiceLoader.load(ILoggerFactory.class);
        List<ILoggerFactory> loggerFactoryList = new LinkedList<>();
        for (ILoggerFactory loggerFactory : loads) {
            loggerFactoryList.add(loggerFactory);
        }
        int loggerFactoryImplNum = loggerFactoryList.size();
        // 唯一绑定
        if (loggerFactoryImplNum == 0) {
            // 未扩展，则使用默认的 LoggerFactory
            DELEGATE = ShoulderLoggerSl4jFactory.getInstance();
        } else {
            // 若扩展则取读到的第一个。注：可以通过扩展修改 logger 缓存大小，减少扩容，增加启动速度
            ILoggerFactory firstLoggerFactory = loggerFactoryList.get(0);
            int implNum = loggerFactoryList.size();
            DELEGATE = firstLoggerFactory;
            System.err.println("Shoulder.LoggerFactory is not unique. Found impl.size = " + implNum +
                ", use the first:" + firstLoggerFactory.getClass().getName());
        }
    }

    /**
     * 获取 logger
     *
     * @param key logger 标识
     * @return logger
     */
    public static Logger getLogger(Class<?> key) {
        return DELEGATE.getLogger(key);
    }

    /**
     * 获取 logger
     *
     * @param key logger 标识
     * @return logger
     */
    public static Logger getLogger(String key) {
        return DELEGATE.getLogger(key);
    }

}
