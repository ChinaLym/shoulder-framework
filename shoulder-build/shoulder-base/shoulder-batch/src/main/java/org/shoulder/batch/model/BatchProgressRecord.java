package org.shoulder.batch.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;


/**
 * 批量处理进度模型 record类
 * <p>
 * 没有进度条、预计剩余时间，已使用时间的等，这些可以通过给的字段计算，因此不给
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@NotThreadSafe
public class BatchProgressRecord implements Serializable {

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
    private int total;

    /**
     * 任务已经处理的记录数
     */
    private int processed;

    /**
     * 成功数
     */
    private int successNum;

    /**
     * 失败数
     */
    private int failNum;

    /**
     * 状态：
     * 0 未开始，1 执行中，2 异常结束，3正常结束
     */
    public int status;

    public void start() {
        status = ProcessStatusEnum.RUNNING.getCode();
        startTime = LocalDateTime.now();
        if (total == 0) {
            finish();
        }
    }

    public void failStop() {
        status = ProcessStatusEnum.EXCEPTION.getCode();
        stopTime = LocalDateTime.now();
    }

    public void finish() {
        assert total == processed;
        status = ProcessStatusEnum.FINISHED.getCode();
        stopTime = LocalDateTime.now();
    }

    public boolean hasFinish() {
        if (status > ProcessStatusEnum.RUNNING.getCode()) {
            return true;
        } else if (total == processed) {
            status = ProcessStatusEnum.FINISHED.getCode();
            return true;
        }
        return false;
    }

    /**
     * 已经花费的时间
     *
     * @return 已经花费的时间
     */
    public long calculateProcessedTime() {
        if (status == ProcessStatusEnum.WAITING.getCode()) {
            return 0;
        }
        if (hasFinish()) {
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
        return this.processed == total ? 0.999F : this.processed / (float) total;
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
        if (processed == 0) {
            // 默认 15分钟
            return 900_000;
        }
        return (calculateProcessedTime() / processed * (total - processed));
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

    public void addSuccess(int successNum) {
        this.successNum += successNum;
        addProcessed(successNum);
    }

    public void addFail(int failNum) {
        this.failNum += failNum;
        addProcessed(failNum);
    }

    private void addProcessed(int processedNum) {
        this.processed += processedNum;
        if (processed == total) {
            finish();
        }
    }

}
