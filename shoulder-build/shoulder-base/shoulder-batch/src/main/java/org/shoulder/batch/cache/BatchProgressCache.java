package org.shoulder.batch.cache;

import org.shoulder.batch.model.BatchProgress;
import org.shoulder.batch.service.impl.ProgressAble;

/**
 * 任务进度存储
 *
 * @author lym
 */
public interface BatchProgressCache {

    /**
     * 触发异步刷进度
     *
     * @param task 需要被刷进度的 task
     */
    void triggerFlushProgress(ProgressAble task);

    /**
     * 获取任务进度
     *
     * @param id taskId
     * @return 任务
     */
    BatchProgress getTaskProgress(String id);


}
