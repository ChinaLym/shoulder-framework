package org.shoulder.batch.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 批量处理进度模型
 * <p>
 * 没有进度条、预计剩余时间，已使用时间的等，这些可以通过给的字段计算，因此不给
 *
 * @author lym
 */
@Data
@NoArgsConstructor
public class BatchProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标识
     */
    private String taskId;

    /**
     * 任务开始执行的时间
     */
    private long startTime;

    /**
     * 任务停止时间
     */
    private long stopTime;

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
    private int success;

    /**
     * 失败数
     */
    private int fail;

    /**
     * 状态：
     * 0 未开始，1 执行中，2 异常结束，3正常结束
     */
    public int status;

    public void start() {
        status = 1;
        startTime = System.currentTimeMillis();
        if (total == 0) {
            finish();
        }
    }

    public void failStop() {
        status = 2;
        stopTime = System.currentTimeMillis();
    }

    public void finish() {
        assert total == processed;
        status = 3;
        stopTime = System.currentTimeMillis();
    }

    public boolean hasFinish() {
        if (status > 1) {
            return true;
        } else if (total == processed) {
            status = 3;
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
        if (hasFinish()) {
            return stopTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
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
        this.success += successNum;
        addProcessed(successNum);
    }

    public void addFail(int failNum) {
        this.success += failNum;
        addProcessed(failNum);
    }

    private void addProcessed(int processedNum) {
        this.processed += processedNum;
        if (processed == total) {
            finish();
        }
    }

}
