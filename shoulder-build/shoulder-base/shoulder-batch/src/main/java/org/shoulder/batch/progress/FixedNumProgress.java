package org.shoulder.batch.progress;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.batch.model.ProcessStatusEnum;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;


/**
 * 批量处理进度并发模型
 * <p>
 * 额外附带了子任务幂等能力，适用于已经确定子任务数目的程序，且子任务id是从0连续有序的，只能记录完成数，不能记录失败数目
 * 线程不安全：建议通过加锁保证
 *
 * @author lym
 */
@Data
@NoArgsConstructor
//@javax.annotation.concurrent.NotThreadSafe
public class FixedNumProgress implements Serializable, ProgressAble {

    private static final long serialVersionUID = 1L;

    private boolean autoFished = true;

    private BiConsumer<String, ProgressAble> onFinishCallback = ProgressAble.super::onFinished;

    /**
     * 任务标识
     */
    private String taskId;

    /**
     * 任务开始执行的时间（若出现挂起，指最后一次开始运行的时间）任务创建时间不在进度中体现
     */
    private LocalDateTime startTime;

    /**
     * 任务停止时间
     */
    private LocalDateTime stopTime;

    /**
     * 实际是 final
     * 空构造器只用于序列化/反序列化
     */
    private BitSet set;

    private int total;

    /**
     * 状态：
     * 0 未开始，1 执行中，2 异常结束，3正常结束
     */
    private AtomicInteger status = new AtomicInteger();

    private Map<String, Object> ext;

    public FixedNumProgress(int totalNum, boolean autoFished) {
        this.total = totalNum;
        this.set = new BitSet(totalNum);
        this.autoFished = autoFished;
    }


    @Override
    public void start() {
        // 只能从未开始到开始
        AssertUtils.notBlank(taskId, CommonErrorCodeEnum.ILLEGAL_STATUS);
        AssertUtils.isTrue(status.compareAndSet(ProcessStatusEnum.WAITING.getCode(), ProcessStatusEnum.RUNNING.getCode()), CommonErrorCodeEnum.ILLEGAL_STATUS);
        startTime = LocalDateTime.now();
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public void failStop() {
        status.getAndSet(ProcessStatusEnum.EXCEPTION.getCode());
        stopTime = LocalDateTime.now();
    }

    @Override
    public void finish() {
        status.getAndSet(ProcessStatusEnum.FINISHED.getCode());
        stopTime = LocalDateTime.now();
    }

    @Override
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
    @Override
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
    @Override
    public float calculateProgress() {
        if (hasFinish()) {
            return 1;
        }
        // 即将完成 99%
        int process = this.set.cardinality();
        int totalNum = this.total;
        return process == totalNum ? 0.999F : process / (float) totalNum;
    }

    /**
     * 估算剩余所需时间
     *
     * @return 剩余所需时间 ms
     */
    @Override
    public long calculateTimeLeft() {
        if (hasFinish()) {
            return 0L;
        }
        int processedNum = this.set.cardinality();
        int totalNum = total;
        if (processedNum <= 0) {
            // 默认 99 天
            return Duration.ofDays(99).toMillis();
        }
        return (calculateProcessedTime() / (processedNum) * (totalNum - processedNum));
    }

    @Override
    public String toString() {
        return "BatchProgress{" +
                "taskId='" + taskId + '\'' +
                ", total=" + total +
                ", processed=" + this.set.cardinality() +
                ", startTime=" + startTime +
                ", status=" + status +
                '}';
    }

    public BatchProgressRecord toRecord() {
        // set 顺序按照不易变化 -> 易变化顺序设置，为了逻辑严谨部分字段内容、顺序单独处理
        BatchProgressRecord record = new BatchProgressRecord();
        record.setTaskId(taskId);
        record.setStartTime(startTime);
        record.setAlreadyFinishedAtStart(0);
        record.setStatus(status.get());
        record.setStopTime(stopTime);
        record.setFailNum(0);
        record.setSuccessNum(this.set.cardinality());
        record.setProcessed(record.getSuccessNum() + record.getFailNum());
        record.setTotal(total);
        record.setExt(ext);
        return record;
    }

    @Override
    public BatchProgressRecord getBatchProgress() {
        return toRecord();
    }

    @Override
    public void finishPart(int partIndex) {
        synchronized (this.set) {
            this.set.set(partIndex);
            checkFinished();
        }
    }

    @Override
    public void onFinished(String id, ProgressAble task) {
        onFinishCallback.accept(id, task);
    }

    private void checkFinished() {
        if (set.cardinality() == set.cardinality() && autoFished) {
            finish();
        }
    }

}
