package org.shoulder.batch.progress;

import java.util.function.BiConsumer;

/**
 * 有进度的
 *
 * @author lym
 */
public interface Progress extends ProgressAble {

    /**
     * 只能从未开始到开始
     *
     * @return 如果任务未开始，则开始任务，返回 true；其他情况返回 false
     */
    boolean start();

    void failStop();

    /**
     * 尝试设置为完成
     *
     * @return 如果任务完成返回 true，其他返回 false
     */
    boolean finish();

    boolean hasFinish();

    long calculateProcessedTime();

    float calculateProgress();

    /**
     * 剩余时间
     * @return -1 无法预估，0 未开始，其余正常
     */
    long calculateTimeLeft();

    /**
     * 任务进度
     *
     * @return 进度
     */
    BatchProgressRecord toProgressRecord();

    String getId();

    /**
     * 完成第 partIndex 个分片
     *
     * @param partIndex 分片标
     */
    void finishPart(int partIndex);


    void setTotal(int total);

    default boolean setTotalAndStart(int total) {
        setTotal(total);
        return start();
    }

    void setOnFinishCallback(BiConsumer<String, Progress> onFinishedCallback);
}
