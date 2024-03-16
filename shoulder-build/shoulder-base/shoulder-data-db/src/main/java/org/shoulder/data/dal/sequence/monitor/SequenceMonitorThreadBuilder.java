package org.shoulder.data.dal.sequence.monitor;

import org.shoulder.data.dal.sequence.XDataSource;
import org.shoulder.data.dal.sequence.ZdalAttributesConfig;
import org.shoulder.data.dal.sequence.dao.SequenceDao;
import org.shoulder.data.dal.sequence.dao.SequenceRangeCache;

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
    ZdalAttributesConfig attributesConfig = new ZdalAttributesConfig();

    String appDataSourceName;

    private SequenceMonitorThreadBuilder() {
    }

    public static SequenceMonitorThreadBuilder build(XDataSource zdalDataSource,
                                                     SequenceRangeCache sequenceRangeCache,
                                                     ConcurrentHashMap<String, Semaphore> sequenceSemaphoreMap,
                                                     SequenceDao sequenceDao) {
        SequenceMonitorThreadBuilder builder = new SequenceMonitorThreadBuilder();

        builder.sequenceRangeCache = sequenceRangeCache;
        builder.sequenceSemaphoreMap = sequenceSemaphoreMap;
        builder.sequenceDao = sequenceDao;

        builder.appDataSourceName = "DEFAULT_DATASOURCE_NAME";//zdalDataSource.getAppDataSourceName();
        //builder.attributesConfig = //zdalDataSource.getZdalDataSourceConfig().getAttributesConfig();
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
