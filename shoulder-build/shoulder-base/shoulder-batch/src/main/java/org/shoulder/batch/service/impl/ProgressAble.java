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

}
