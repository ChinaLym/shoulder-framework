package org.shoulder.batch.cache;

import org.shoulder.batch.model.BatchProgress;
import org.shoulder.batch.service.impl.ProgressAble;

/**
 * 批处理任务进度缓存
 *
 * @author lym
 */
public interface BatchProgressCache {

    /**
     * 获取任务进度
     *
     * @param id taskId
     * @return 任务
     */
    BatchProgress getTaskProgress(String id);

    /**
     * 触发异步刷进度（直接刷）
     *
     * @param batchProgress 进度
     */
    void triggerFlushProgress(BatchProgress batchProgress);

    /**
     * 触发异步刷进度（需要动态获取进度）
     *
     * @param task 需要被刷进度的 task
     */
    void triggerFlushProgress(ProgressAble task);


}
