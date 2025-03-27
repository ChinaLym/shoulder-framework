package org.shoulder.batch.progress;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;

import java.io.Serial;
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
public class FixedNumProgress implements Serializable, Progress {

    @Serial private static final long serialVersionUID = 1L;

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
    public boolean start() {
        AssertUtils.notBlank(id, CommonErrorCodeEnum.ILLEGAL_STATUS);
        boolean started = status.compareAndSet(ProgressStatusEnum.WAITING.getCode(), ProgressStatusEnum.RUNNING.getCode());
        if (!started) {
            return false;
        }
        startTime = LocalDateTime.now();
        return true;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public void failStop() {
        status.getAndSet(ProgressStatusEnum.EXCEPTION.getCode());
        stopTime = LocalDateTime.now();
    }

    @Override
    public void finish() {
        status.getAndSet(ProgressStatusEnum.FINISHED.getCode());
        stopTime = LocalDateTime.now();
        if (startTime == null) {
            startTime = stopTime;
        }
    }

    @Override
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
    @Override
    public long calculateProcessedTime() {
        if (status.get() == ProgressStatusEnum.WAITING.getCode()) {
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
                "batchId='" + id + '\'' +
                ", total=" + total +
                ", processed=" + this.set.cardinality() +
                ", startTime=" + startTime +
                ", status=" + status +
                '}';
    }

    public BatchProgressRecord toRecord() {
        // set 顺序按照不易变化 -> 易变化顺序设置，为了逻辑严谨部分字段内容、顺序单独处理
        BatchProgressRecord record = new BatchProgressRecord();
        record.setId(id);
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
    public BatchProgressRecord toProgressRecord() {
        return toRecord();
    }

    @Override
    public void finishPart(int partIndex) {
        synchronized (this.set) {
            this.set.set(partIndex);
            checkFinished(autoFished);
        }
    }

    @Override
    public void onFinished(String id, ProgressAble task) {
        onFinishCallback.accept(id, (Progress) task);
    }

    private void checkFinished(boolean autoFished) {
        if (set.cardinality() == set.size() && autoFished) {
            finish();
        }
    }

}
