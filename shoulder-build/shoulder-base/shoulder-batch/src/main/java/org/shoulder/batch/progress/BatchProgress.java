package org.shoulder.batch.progress;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.batch.model.ProcessStatusEnum;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


/**
 * 批量处理进度并发模型
 * <p>
 * 没有进度条、预计剩余时间，已使用时间的等，这些可以通过给的字段计算，因此不给
 * 线程安全：支持任务总数动态调整、支持单机多线程管理进度
 *
 * @author lym
 */
@Data
@NoArgsConstructor
//@javax.annotation.concurrent.ThreadSafe
public class BatchProgress implements Serializable, ProgressAble {

    private static final long serialVersionUID = 1L;

    private boolean autoFished = true;

    private BiConsumer<String, ProgressAble> onFinishCallback = ProgressAble.super::onFinished;

    /**
     * 批处理任务id
     */
    private String id;

    /**
     * 任务开始执行的时间（若出现挂起，指最后一次开始运行的时间）任务创建时间不在进度中体现
     */
    private LocalDateTime startTime;

    /**
     * 如：上次运行时已经完成一部分了，在这里体现，用于预估结束时间
     */
    private int alreadyFinishedAtStart;

    /**
     * 任务停止时间
     */
    private LocalDateTime stopTime;

    /**
     * 任务需要处理的记录总数
     */
    private AtomicInteger total = new AtomicInteger();

    /**
     * 任务已经处理的记录数
     */
    private AtomicInteger processed = new AtomicInteger();

    /**
     * 成功数
     */
    private AtomicInteger successNum = new AtomicInteger();

    /**
     * 失败数
     */
    private AtomicInteger failNum = new AtomicInteger();

    /**
     * 状态：
     * 0 未开始，1 执行中，2 异常结束，3正常结束
     */
    private AtomicInteger status = new AtomicInteger();

    private Map<String, Object> ext;

    public BatchProgress(boolean autoFished) {
        this.autoFished = autoFished;
    }

    public void setAlreadyFinishedAtStart(int alreadyFinishedAtStart) {
        this.alreadyFinishedAtStart = alreadyFinishedAtStart;
    }

    public void start() {
        // 只能从未开始到开始
        startTime = LocalDateTime.now();
    }

    public void setTotal(int total) {
        this.total.getAndSet(total);
    }

    public void failStop() {
        status.getAndSet(ProcessStatusEnum.EXCEPTION.getCode());
        stopTime = LocalDateTime.now();
    }

    public void finish() {
        status.getAndSet(ProcessStatusEnum.FINISHED.getCode());
        stopTime = LocalDateTime.now();
    }

    public boolean hasFinish() {
        if (status.get() > ProcessStatusEnum.RUNNING.getCode()) {
            return true;
        }
        checkFinished();
        return status.get() > ProcessStatusEnum.RUNNING.getCode();
    }

    /**
     * 已经花费的时间
     *
     * @return 已经花费的时间
     */
    public long calculateProcessedTime() {
        if (status.get() == ProcessStatusEnum.WAITING.getCode()) {
            return 0;
        }
        if (hasFinish()) {
            if (stopTime == null) {
                stopTime = LocalDateTime.now();
            }
            return Duration.between(startTime, stopTime).toMillis();
        }
        return Duration.between(startTime, LocalDateTime.now()).toMillis();
    }

    /**
     * 估算当前进度
     *
     * @return 进度
     */
    public float calculateProgress() {
        if (hasFinish()) {
            return 1;
        }
        // 即将完成 99%
        int process = this.processed.get();
        int totalNum = this.total.get();
        return process == totalNum ? 0.999F : process / (float) totalNum;
    }

    /**
     * 估算剩余所需时间
     *
     * @return 剩余所需时间 ms
     */
    public long calculateTimeLeft() {
        if (hasFinish()) {
            return 0L;
        }
        int processedNum = processed.get();
        int totalNum = total.get();
        if (processedNum - alreadyFinishedAtStart <= 0) {
            // 默认 99 天
            return Duration.ofDays(99).toMillis();
        }
        return (calculateProcessedTime() / (processedNum - alreadyFinishedAtStart) * (totalNum - processedNum));
    }

    @Override
    public String toString() {
        return "BatchProgress{" +
               "batchId='" + id + '\'' +
               ", total=" + total +
               ", processed=" + processed +
               ", startTime=" + startTime +
               ", status=" + status +
               '}';
    }

    public void addSuccess(int num) {
        this.successNum.addAndGet(num);
        addProcessed(num);
    }

    public void addFail(int num) {
        this.failNum.addAndGet(num);
        addProcessed(num);
    }

    public BatchProgressRecord toRecord() {
        // set 顺序按照不易变化 -> 易变化顺序设置，为了逻辑严谨部分字段内容、顺序单独处理
        BatchProgressRecord record = new BatchProgressRecord();
        record.setId(id);
        record.setStartTime(startTime);
        record.setAlreadyFinishedAtStart(alreadyFinishedAtStart);
        record.setStatus(status.get());
        record.setStopTime(stopTime);
        record.setFailNum(failNum.get());
        record.setSuccessNum(successNum.get());
        record.setProcessed(record.getSuccessNum() + record.getFailNum());
        record.setTotal(total.get());
        record.setExt(ext);
        return record;
    }

    @Override
    public BatchProgressRecord getBatchProgress() {
        return toRecord();
    }

    @Override
    public void finishPart(int partIndex) {
        addSuccess(1);
    }

    @Override
    public void onFinished(String id, ProgressAble task) {
        onFinishCallback.accept(id, task);
    }

    private void addProcessed(int processedNum) {
        this.processed.addAndGet(processedNum);
        checkFinished();
    }

    private void checkFinished() {
        if (processed.get() == total.get() && autoFished) {
            finish();
        }
    }

}
