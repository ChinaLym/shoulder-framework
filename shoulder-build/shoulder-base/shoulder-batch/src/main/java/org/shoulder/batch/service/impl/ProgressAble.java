package org.shoulder.batch.service.impl;

import org.shoulder.batch.model.BatchProgress;

/**
 * 有进度的
 *
 * @author lym
 */
public interface ProgressAble {

    /**
     * 任务标识
     *
     * @return taskId
     */
    String getTaskId();

    /**
     * 任务进度
     *
     * @return 进度
     */
    BatchProgress getBatchProgress();

}
