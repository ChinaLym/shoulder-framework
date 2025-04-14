package org.shoulder.batch.progress;

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
     * @param progressId progressId
     * @return 任务
     */
    Progress findProgress(String progressId);

    /**
     * 获取所有任务id
     *
     * @return 所有任务id
     */
    Iterable<String> fetchAllTaskProgressId();

    /**
     * 获取所有任务进度
     *
     * @return 所有任务进度
     */
    Map<String, Progress> fetchAllTaskProgress();

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

    /**
     * remove special
     */
    void evict(String id);

    /**
     * clean all
     */
    void clear();
}
