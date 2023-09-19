package org.shoulder.batch.service.impl;

import org.shoulder.batch.model.BatchProgressRecord;

import java.util.function.BiConsumer;

/**
 * 有进度的
 *
 * @author lym
 */
public interface ProgressAble {

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

    String getTaskId();

    /**
     * 完成第 partIndex 个分片
     *
     * @param partIndex 分片标
     */
    void finishPart(int partIndex);

    /**
     * 结束后的回调
     */
    default void onFinished(String id, ProgressAble task) {


    }

    void setTotal(int total);

    void setOnFinishCallback(BiConsumer<String, ProgressAble> onFinishedCallback);
}
