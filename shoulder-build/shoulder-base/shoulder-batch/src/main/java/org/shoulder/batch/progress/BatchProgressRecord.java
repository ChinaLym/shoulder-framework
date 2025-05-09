package org.shoulder.batch.progress;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiConsumer;


/**
 * 批量处理进度模型 record类
 * <p>
 * 没有进度条、预计剩余时间，已使用时间的等，这些可以通过给的字段计算，因此不给
 * 线程不安全：适用于单机单线程管理进度的模块，绝大多数场景足够
 *
 * @author lym
 * fixme 完成了 stopTime 为空，显示还剩1个未完成
 */
@Data
@NoArgsConstructor
//@javax.annotation.concurrent.NotThreadSafe
public class BatchProgressRecord implements Serializable, Progress {

    @Serial private static final long serialVersionUID = 1L;

    /**
     * 批处理任务id
     */
    private String id;

    /**
     * 任务开始执行的时间
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

    private Map<String, Object> ext;

    @Override
    public boolean start() {
        if (status != ProgressStatusEnum.WAITING.getCode()) {
            return false;
        }
        status = ProgressStatusEnum.RUNNING.getCode();
        startTime = LocalDateTime.now();
        if (total == 0) {
            finish();
        }
        return true;
    }

    @Override
    public void failStop() {
        status = ProgressStatusEnum.EXCEPTION.getCode();
        stopTime = LocalDateTime.now();
    }

    @Override
    public boolean finish() {
        AssertUtils.equals(total, processed, CommonErrorCodeEnum.ILLEGAL_STATUS);
        status = ProgressStatusEnum.FINISHED.getCode();
        stopTime = LocalDateTime.now();
        if (startTime == null) {
            startTime = stopTime;
        }
        return true;
    }

    public boolean hasFinish() {
        return status > ProgressStatusEnum.RUNNING.getCode();
    }

    /**
     * 已经花费的时间
     *
     * @return 已经花费的时间
     */
    public long calculateProcessedTime() {
        if (status == ProgressStatusEnum.WAITING.getCode()) {
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
        if (processed - alreadyFinishedAtStart <= 0) {
            // 默认 99 天
            return Duration.ofDays(99).toMillis();
        }
        return (calculateProcessedTime() / (processed - alreadyFinishedAtStart) * (total - processed));
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

    @Override
    public BatchProgressRecord toProgressRecord() {
        return this;
    }

    @Override
    public void finishPart(int partIndex) {
        addSuccess(1);
    }

    @Override
    public void setOnFinishCallback(BiConsumer<String, Progress> onFinishedCallback) {
        throw new UnsupportedOperationException("static record object not supported!");
    }

}
