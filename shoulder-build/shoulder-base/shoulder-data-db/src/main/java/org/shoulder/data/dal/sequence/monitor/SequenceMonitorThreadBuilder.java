package org.shoulder.data.dal.sequence.monitor;

import org.shoulder.data.dal.sequence.dao.SequenceDao;
import org.shoulder.data.dal.sequence.model.SequenceRangeCache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * 构建后台监控线程
 *
 * @author lym
 */
public class SequenceMonitorThreadBuilder {

    SequenceRangeCache sequenceRangeCache;
    ConcurrentHashMap<String, Semaphore> sequenceSemaphoreMap;
    SequenceDao sequenceDao;

    String appDataSourceName;

    public static SequenceMonitorThreadBuilder build(SequenceRangeCache sequenceRangeCache, ConcurrentHashMap<String, Semaphore> sequenceSemaphoreMap, SequenceDao sequenceDao) {
        SequenceMonitorThreadBuilder builder = new SequenceMonitorThreadBuilder();

        builder.sequenceRangeCache = sequenceRangeCache;
        builder.sequenceSemaphoreMap = sequenceSemaphoreMap;
        builder.sequenceDao = sequenceDao;
        // 后续多数据源该值也需要监控
        builder.appDataSourceName = "DEFAULT_DATASOURCE_NAME";
        return builder;
    }

    public void start() {
        Thread refreshThread = new Thread(new SequenceRefreshRunnable(this));
        refreshThread.setName("SEQ-REFRESH-THREAD-" + appDataSourceName);
        refreshThread.setDaemon(true);
        refreshThread.start();

        Thread logPrinterThread = new Thread(new SequenceLogPrintRunnable(this));
        logPrinterThread.setName("SEQ-LOG-PRINT-THREAD-" + appDataSourceName);
        logPrinterThread.setDaemon(true);
        logPrinterThread.start();
    }

}
