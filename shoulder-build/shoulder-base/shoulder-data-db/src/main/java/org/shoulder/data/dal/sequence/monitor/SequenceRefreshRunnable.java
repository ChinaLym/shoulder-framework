package org.shoulder.data.dal.sequence.monitor;

import org.shoulder.core.context.AppContext;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.data.dal.sequence.model.DoubleSequenceRange;
import org.shoulder.data.dal.sequence.model.SequenceRange;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * 后台进程，监听序列号缓存状态，并在恰当时机去数据库获取新的一批序列号并换存在内存
 *
 * @author lym
 */
public class SequenceRefreshRunnable implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger("SEQUENCE_MONITOR");

    private SequenceMonitorThreadBuilder builder;

    /**
     * Sequence 刷新间隔时间
     * <p>
     * 默认 10000 ms
     */
    private long sequenceRefreshInterval = 10 * 1000;
    /**
     * Sequence 刷新功能是否开启, 默认不开启
     */
    private boolean sequenceRefresh = false;


    /**
     * Sequence 刷新阈值，当前 SequenceRange 超过这个阈值会刷新下一个 SequenceRange, 默认 30% 时就刷新下一个 SequenceRange
     */
    private double sequenceRefreshThreshold = 0.3;

    public SequenceRefreshRunnable(SequenceMonitorThreadBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("start sequence refresh thread {}", builder.appDataSourceName);
        }

        for (; ; ) {
            try {
                Thread.sleep(sequenceRefreshInterval);
                if (!sequenceRefresh) {
                    continue;
                }

                for (Map.Entry<String, DoubleSequenceRange> entry : builder.sequenceRangeCache.asMap().entrySet()) {
                    // 检查全部缓存
                    String sequenceId = entry.getKey();
                    Semaphore semaphore = builder.sequenceSemaphoreMap.get(sequenceId);
                    // 尝试获得当前 sequence id 的信号量（锁 / 执行权限）
                    if (semaphore.tryAcquire()) {
                        try {
                            DoubleSequenceRange sequenceRange = entry.getValue();
                            SequenceRange currentSequenceRange = sequenceRange.getCurrent();
                            if (currentSequenceRange == null) {
                                // 基本不会发生，可直接等待下次触发刷新
                                continue;
                            }

                            SequenceRange nextSequenceRange = sequenceRange.getNext();
                            if (currentSequenceRange.needRefresh()) {
                                boolean nextAvailable = nextSequenceRange != null && nextSequenceRange.needRefresh();
                                if (nextAvailable) {
                                    // current 恰好用光但 next 有值：切到下一个
                                    sequenceRange.switchNextAndGet();
                                    if (LOGGER.isInfoEnabled()) {
                                        LOGGER.info("doubleBuffer switched: {}", sequenceId);
                                    }
                                } else {
                                    // current/next 恰好都用光了：从DB拿一段并放入双 buffer 缓存
                                    nextSequenceRange = loadNextSequence(sequenceId, currentSequenceRange);
                                    sequenceRange.setAndSwitchNext(nextSequenceRange);
                                    builder.sequenceRangeCache.put(entry.getKey(), sequenceRange); // 更新后刷新下缓存
                                    if (LOGGER.isInfoEnabled()) {
                                        LOGGER.info("doubleBuffer refreshed and switched: {}", sequenceId);
                                    }
                                }
                            } else {
                                // 当前使用率 > 刷新next阈值，且 next 不可用，则进行预刷新，减少拿的时候不够了而触发刷新的情况，宁可浪费一定序列也不要在取的时候阻塞
                                double useRate = (currentSequenceRange.currentValue() - currentSequenceRange.getValue()) * 1.0 / currentSequenceRange.getStep();
                                boolean checkNext = useRate > sequenceRefreshThreshold;
                                if (!checkNext) {
                                    return;
                                }
                                boolean nextAvailable = nextSequenceRange != null && nextSequenceRange.needRefresh();

                                if (nextAvailable) {
                                    return;
                                }
                                // 举例：current 用了 70%，且 next 不可用，则需要提前刷新 next
                                nextSequenceRange = loadNextSequence(sequenceId, currentSequenceRange);
                                sequenceRange.setNext(nextSequenceRange);
                                // 更新后刷新下缓存
                                builder.sequenceRangeCache.put(entry.getKey(), sequenceRange);
                                if (LOGGER.isInfoEnabled()) {
                                    LOGGER.info("doubleBuffer refreshed {}", sequenceId);
                                }
                            }
                        } finally {
                            semaphore.release();
                        }
                    }

                    // 避免配置出错，频繁检查导致 CPU 飙高，最快也要1ms检查一次
                    Thread.sleep(1);
                }
            } catch (Throwable e) {
                LOGGER.error("Refresh Error", e);
            }
        }
    }

    private SequenceRange loadNextSequence(String sequenceId, SequenceRange sequenceRange)
        throws Exception {
        // Map<String, Serializable> allContext = sequenceRange.getAllContext();
        try {
            // 适用外部上下文，如压测标识、分库key等线程变量
            // AppContext.set(allContext);

            return builder.sequenceDao.loadNextSequenceFromDbViaNewTransaction(sequenceId, sequenceRange);
        } finally {
            // 清理所有的线程变量
            AppContext.clean();
        }
    }
}
