package org.shoulder.batch.cache;

import org.shoulder.batch.service.impl.ProgressAble;

import java.util.Map;

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
    ProgressAble getTaskProgress(String id);

    Iterable<String> getAllTaskProgressId();

    Map<String, ProgressAble> getAllTaskProgress();

    /**
     * 刷一次进度
     *
     * @param batchProgress 进度
     */
    void flushProgress(ProgressAble batchProgress);

    /**
     * 触发异步刷进度
     *
     * @param batchProgress 进度;注意需要维护该对象引用
     */
    void triggerFlushProgress(ProgressAble batchProgress);

}
