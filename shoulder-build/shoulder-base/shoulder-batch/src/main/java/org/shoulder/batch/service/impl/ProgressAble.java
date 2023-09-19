package org.shoulder.batch.service.impl;

import org.shoulder.batch.model.BatchProgressRecord;

/**
 * 有进度的
 *
 * @author lym
 */
public interface ProgressAble {

    /**
     * 任务进度
     *
     * @return 进度
     */
    BatchProgressRecord getBatchProgress();

    /**
     * 完成第 partIndex 个分片
     *
     * @param partIndex 分片标
     */
    String getTaskId();

    void finishPart(int partIndex);

    /**
     * 结束后的回调
     */
    default void onFinished(String id, ProgressAble task) {


    }

}
