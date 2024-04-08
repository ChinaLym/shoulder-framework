package org.shoulder.data.context;

import org.shoulder.data.log.ShoulderDBLoggers;
import org.slf4j.Logger;

/**
 * 多数据源上下文存储
 *
 * @author lym
 */
public class DataSourceContextHolder {

    private static final Logger log = ShoulderDBLoggers.DEFAULT;

    /**
     * 数据源上下文
     */
    private static final ThreadLocal<String> LOCAL_DATA_SOURCE_TYPE = new ThreadLocal<>();

    /**
     * 获得数据源的变量
     */
    public static String getDataSourceType() {
        return LOCAL_DATA_SOURCE_TYPE.get();
    }

    /**
     * 设置数据源的变量
     */
    public static void setDataSourceType(String dataSourceType) {
        log.debug("check out dataSource to {}", dataSourceType);
        LOCAL_DATA_SOURCE_TYPE.set(dataSourceType);
    }

    /**
     * 清空数据源变量
     */
    public static void clean() {
        LOCAL_DATA_SOURCE_TYPE.remove();
    }
}
