package org.shoulder.log.operation.logger.impl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PreDestroy;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.model.Operable;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.logger.OperationLoggerInterceptor;
import org.shoulder.log.operation.model.OperationLogDTO;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 带缓冲的操作日志记录器（针对频繁记录单条操作日志做优化，批量操作不优化）
 * 将日志放入 buffer 中，当 buffer 中日志达到一定数量 / 每隔一定时间，执行一次批量保存
 *
 * @author lym
 */
public class BufferedOperationLogger implements OperationLogger {

    /**
     * 需要批量记录的日志都会仍在这里
     */
    private final Queue<OperationLogDTO> logBuffer;

    /**
     * 日志记录器
     */
    private final OperationLogger delegate;

    /**
     * 定时消费日志，定时器，在这里持有其引用
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 当积攒的 buffer 中日志数达到 flushThreshold 条触发一次批量记录，不影响固定扫描间隔
     */
    private final int flushThreshold;

    /**
     * 每次消费上限，大于该值会拆分
     * 推荐根据表结构定制。如统计 Mysql单页可以存几条数据，取该值
     */
    private final int perFlushMax;


    /**
     * 上次记录日志时间
     */
    private final AtomicLong lastLogTime = new AtomicLong(0);

    /**
     * 正在把 buffer 中的数据写入日志
     */
    private final AtomicBoolean flushing = new AtomicBoolean(false);


    /**
     * 记录一条操作日志
     */
    @Override
    public void log(@Nonnull OperationLogDTO opLog) {
        logBuffer.add(opLog);
        int current = logBuffer.size();
        if (current >= flushThreshold) {
            // 达到提前刷的积累数量，尝试触发刷日志
            consumerLog();
        }
    }

    public BufferedOperationLogger(Queue<OperationLogDTO> logBuffer, OperationLogger delegate, ScheduledExecutorService scheduledExecutorService,
                                   long flushInterval, int flushThreshold, int perFlushMax) {
        this.logBuffer = logBuffer;
        this.delegate = delegate;
        this.scheduledExecutorService = scheduledExecutorService;
        this.flushThreshold = flushThreshold;
        this.perFlushMax = perFlushMax;

        // 启动异步刷任务
        scheduledExecutorService.scheduleAtFixedRate(this::consumerLog, flushInterval, flushInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * 记录多条操作日志
     */
    @Override
    public void log(@Nonnull Collection<? extends OperationLogDTO> opLogList) {
        delegate.log(opLogList);
    }

    @Override
    public void log(@Nonnull OperationLogDTO opLog, List<? extends Operable> operableList) {
        delegate.log(opLog, operableList);
    }

    @Override
    public void addInterceptor(OperationLoggerInterceptor logInterceptor) {
        this.delegate.addInterceptor(logInterceptor);
    }

    public OperationLogger getDelegate() {
        return delegate;
    }


    public void consumerLog() {
        if (!flushing.compareAndSet(false, true)) {
            // 已经有人在刷了，让他接着做
            return;
        }
        // 只有一个线程可以运行到这
        OperationLogDTO temp;
        List<OperationLogDTO> opLogList = new LinkedList<>();
        while ((temp = logBuffer.poll()) != null) {
            opLogList.add(temp);
            if (opLogList.size() == perFlushMax) {
                delegate.log(opLogList);
                lastLogTime.set(System.currentTimeMillis());
                opLogList = new LinkedList<>();
            }
        }
        if (CollectionUtils.isNotEmpty(opLogList)) {
            delegate.log(opLogList);
            lastLogTime.set(System.currentTimeMillis());
        }
        flushing.set(false);
    }

    @PreDestroy
    public void preDestroy() {
        // Bean 注销前，消费掉累积的日志
        consumerLog();
    }

}
