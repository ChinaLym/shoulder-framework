package org.shoulder.batch.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.batch.service.impl.ProgressAble;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * 批量处理进度并发模型
 * <p>
 * 没有进度条、预计剩余时间，已使用时间的等，这些可以通过给的字段计算，因此不给
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@ThreadSafe
public class BatchProgress implements Serializable, ProgressAble {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标识
     */
    private String taskId;

    /**
     * 任务开始执行的时间
     */
    private LocalDateTime startTime;

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

    public void start() {
        // 只能从未开始到开始
        AssertUtils.notBlank(taskId, CommonErrorCodeEnum.ILLEGAL_STATUS);

        AssertUtils.isTrue(status.compareAndSet(ProcessStatusEnum.WAITING.getCode(), ProcessStatusEnum.RUNNING.getCode()), CommonErrorCodeEnum.ILLEGAL_STATUS);
        startTime = LocalDateTime.now();
//        if (total.get() == 0) {
//            AssertUtils.isTrue(status.compareAndSet(1, 3), CommonErrorCodeEnum.ILLEGAL_STATUS);
//        }
    }

    public int setTotal(int total) {
        return this.total.getAndSet(total);
    }

    public int failStop() {
        int oldStatus = status.getAndSet(ProcessStatusEnum.EXCEPTION.getCode());
        stopTime = LocalDateTime.now();
        return oldStatus;
    }

    public int finish() {
        AssertUtils.equals(processed.get(), total.get(), CommonErrorCodeEnum.ILLEGAL_STATUS);
        int oldStatus = status.getAndSet(ProcessStatusEnum.EXCEPTION.getCode());
        stopTime = LocalDateTime.now();
        return oldStatus;
    }

    public boolean hasFinish() {
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
        if (processedNum == 0) {
            // 默认 30 分钟
            return Duration.of(30, ChronoUnit.MINUTES).toMillis();
        }
        return (calculateProcessedTime() / processedNum * (totalNum - processedNum));
    }

    @Override
    public String toString() {
        return "BatchProgress{" +
                "taskId='" + taskId + '\'' +
                ", total=" + total +
                ", processed=" + processed +
                ", startTime=" + startTime +
                ", status=" + status +
                '}';
    }

    public void addSuccess(int num) {
        this.successNum.addAndGet(num);
        this.processed.addAndGet(num);
    }

    public void addFail(int num) {
        this.failNum.addAndGet(num);
        this.processed.addAndGet(num);
    }

    public BatchProgressRecord toRecord() {
        // set 顺序按照不易变化 -> 易变化顺序设置，为了逻辑严谨部分字段内容、顺序单独处理
        BatchProgressRecord record = new BatchProgressRecord();
        record.setTaskId(taskId);
        record.setStartTime(startTime);
        record.setStopTime(stopTime);
        record.setStatus(status.get());
        record.setFailNum(failNum.get());
        record.setSuccessNum(successNum.get());
        record.setProcessed(record.getSuccessNum() + record.getFailNum());
        record.setTotal(total.get());
        return record;
    }

    @Override
    public BatchProgressRecord getBatchProgress() {
        return toRecord();
    }
}
