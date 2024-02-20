package org.shoulder.batch.progress;

import java.util.function.BiConsumer;

/**
 * 有进度的
 *
 * @author lym
 */
public interface Progress extends ProgressAble {

    void start();

    void failStop();

    void finish();

    boolean hasFinish();

    long calculateProcessedTime();

    float calculateProgress();

    long calculateTimeLeft();

    /**
     * 任务进度
     *
     * @return 进度
     */
    BatchProgressRecord getBatchProgress();

    String getId();

    /**
     * 完成第 partIndex 个分片
     *
     * @param partIndex 分片标
     */
    void finishPart(int partIndex);


    void setTotal(int total);

    void setOnFinishCallback(BiConsumer<String, Progress> onFinishedCallback);
}
