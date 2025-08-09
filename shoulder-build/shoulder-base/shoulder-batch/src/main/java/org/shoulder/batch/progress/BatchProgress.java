package org.shoulder.batch.progress;

import com.google.errorprone.annotations.ThreadSafe;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;

import java.io.Serial;
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
@ThreadSafe
public class BatchProgress implements Serializable, Progress {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * 在判断是否完成 / success / failNumber 变化时，是否自动根据处理进度判断结束
     * 如果total在任务开始前不确定或开始后会动态变化，建议设置为 false
     */
    private boolean autoFished = true;

    private BiConsumer<String, Progress> onFinishCallback = Progress.super::onFinished;

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

    @Override
    public boolean start() {
        // 只能从未开始到开始
        boolean started = status.compareAndSet(ProgressStatusEnum.WAITING.getCode(), ProgressStatusEnum.RUNNING.getCode());
        if (!started) {
            return status.get() == ProgressStatusEnum.RUNNING.getCode();
        }
        startTime = LocalDateTime.now();
        return true;
    }

    /**
     * @deprecated 不建议直接set，建议使用 getTotal().compareAndSet() / increment ..
     */
    public void setTotal(int total) {
        AssertUtils.isTrue(status.get() < ProgressStatusEnum.EXCEPTION.getCode(), CommonErrorCodeEnum.ILLEGAL_STATUS);
        this.total.getAndSet(total);
    }

    public void enableAutoFinished() {
        this.autoFished = true;
        checkFinished(true);
    }

    public void failStop() {
        int currentStatus;
        // 只允许 WAITING、RUNNING 进入失败
        do {
            currentStatus = status.get();
            if (currentStatus >= ProgressStatusEnum.EXCEPTION.getCode()) {
                throw CommonErrorCodeEnum.ILLEGAL_STATUS.toException();
            }
        } while (!status.compareAndSet(currentStatus, ProgressStatusEnum.EXCEPTION.getCode()));
        stopTime = LocalDateTime.now();
    }

    public boolean finish() {
        if(processed.get() != total.get()) {
            return status.get() == ProgressStatusEnum.FINISHED.getCode();
        }
        if (status.compareAndSet(ProgressStatusEnum.WAITING.getCode(), ProgressStatusEnum.FINISHED.getCode()) ||
                status.compareAndSet(ProgressStatusEnum.RUNNING.getCode(), ProgressStatusEnum.FINISHED.getCode())) {
            stopTime = LocalDateTime.now();
            if (startTime == null) {
                startTime = stopTime;
            }
            return true;
        }
        return status.get() == ProgressStatusEnum.FINISHED.getCode();
    }

    public boolean hasFinish() {
        if (status.get() > ProgressStatusEnum.RUNNING.getCode()) {
            return true;
        }
        checkFinished(autoFished);
        return status.get() > ProgressStatusEnum.RUNNING.getCode();
    }

    /**
     * 已经花费的时间
     *
     * @return 已经花费的时间
     */
    public long calculateProcessedTime() {
        if (status.get() == ProgressStatusEnum.WAITING.getCode()) {
            // 没开始处理，没花时间
            return 0;
        }
        LocalDateTime endTime = hasFinish() ? (stopTime != null ? stopTime : LocalDateTime.now()) : LocalDateTime.now();
        return Duration.between(startTime, endTime).toMillis();
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
        return totalNum == 0 ? 0 : Math.min(0.999F, process / (float) totalNum);
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
            // 无法预估
            return -1;
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

    @Override
    public BatchProgressRecord toProgressRecord() {
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
    public void finishPart(int partIndex) {
        addSuccess(1);
    }

    @Override
    public void onFinished(String id, ProgressAble task) {
        onFinishCallback.accept(id, (Progress) task);
    }

    private void addProcessed(int processedNum) {
        this.processed.addAndGet(processedNum);
        checkFinished(autoFished);
    }

    public void checkFinished(boolean autoFished) {
        // 先取 processed 再取 total
        int currentProcessed = processed.get();
        int currentTotal = total.get();
        if (currentProcessed == currentTotal && autoFished) {
            finish();
        }
    }

}
