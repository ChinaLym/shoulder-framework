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
    private static ILoggerFactory delegate = null;

    static {
        initLoggerFactoryImpl();
    }

    /**
     * 绑定日志工厂
     */
    private static void initLoggerFactoryImpl(){
        ServiceLoader<ILoggerFactory> loads = ServiceLoader.load(ILoggerFactory.class);
        List<ILoggerFactory> loggerFactoryList = new LinkedList<>();
        for (ILoggerFactory loggerFactory : loads) {
            loggerFactoryList.add(loggerFactory);
        }
        int loggerFactoryImplNum = loggerFactoryList.size();
        // 唯一绑定
        if(loggerFactoryImplNum == 0){
            // 未扩展，则使用默认的 LoggerFactory
            //System.out.println("Shoulder LoggerFactory: use default LoggerFactory[" + ShoulderLoggerSl4jFactory.class.getName() + "]");
            delegate = ShoulderLoggerSl4jFactory.getInstance();
        }else {
            // 否则取第一个
            ILoggerFactory firstLoggerFactory = loggerFactoryList.get(0);
            System.err.println("Shoulder LoggerFactory is not unique. Found impl.size = " + loggerFactoryList.size() +
                    ", use the first:" + firstLoggerFactory.getClass().getName());
            delegate = firstLoggerFactory;
        }
    }

    /**
     * 获取 logger
     *
     * @param key logger 标识
     * @return logger
     */
    public static Logger getLogger(Class<?> key) {
        return delegate.getLogger(key);
    }

    /**
     * 获取 logger
     *
     * @param key logger 标识
     * @return logger
     */
    public static Logger getLogger(String key) {
        return delegate.getLogger(key);
    }

}
