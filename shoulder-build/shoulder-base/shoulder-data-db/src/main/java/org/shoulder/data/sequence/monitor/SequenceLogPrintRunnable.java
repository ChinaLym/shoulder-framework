package org.shoulder.data.sequence.monitor;

import org.shoulder.core.log.LoggerFactory;
import org.shoulder.data.sequence.model.DoubleSequenceRange;
import org.shoulder.data.sequence.model.SequenceRange;
import org.slf4j.Logger;

import java.util.Map;

/**
 * 定期将当 sequence 组件状态以日志形式输出
 *
 * @author lym
 */
public class SequenceLogPrintRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("SEQUENCE_MONITOR");
    private SequenceMonitorThreadBuilder builder;

    public SequenceLogPrintRunnable(SequenceMonitorThreadBuilder builder) {
        this.builder = builder;
    }

    /**
     * Sequence 日志打印间隔时间
     *
     * 默认 30000 ms
     */
    private long                                          sequenceLogPrintInterval       = 30 * 1000;


    @Override
    public void run() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("start sequence log print thread {}", builder.appDataSourceName);
        }

        for (; ; ) {
            try {
                Thread.sleep(sequenceLogPrintInterval);

                for (Map.Entry<String, DoubleSequenceRange> entry : builder.sequenceRangeCache.asMap().entrySet()) {
                    String sequenceId = entry.getKey();
                    SequenceRange current = entry.getValue().getCurrent();
                    if (current == null) {
                        LOGGER.warn("sequence log print thread current sequence range null {}",
                            sequenceId);
                        continue;
                    }

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER
                            .info(
                                "name:{},local:{},value:{},latestValue:{},min:{},max:{},step:{},index:{}",
                                sequenceId, current.currentValue(), current.getValue(),
                                current.getLatestValue(), current.getMin(), current.getMax(),
                                current.getStep(), entry.getValue().getIndex());
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("Fail to log sequence status.", e);
            }
        }
    }
}
