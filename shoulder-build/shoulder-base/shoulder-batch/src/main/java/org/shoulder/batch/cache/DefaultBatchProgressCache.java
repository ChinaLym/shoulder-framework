package org.shoulder.batch.cache;

import org.shoulder.batch.model.BatchProgress;
import org.shoulder.batch.service.impl.ProgressAble;
import org.shoulder.core.concurrent.Threads;
import org.springframework.cache.Cache;

import java.util.concurrent.TimeUnit;

/**
 * 任务进度存储
 *
 * @author lym
 */
public class DefaultBatchProgressCache implements BatchProgressCache {

    /**
     * 允许进度以多种方式保存在其他地方，如 redis
     */
    private final Cache importProgressCache;

    /**
     * 缓存名
     */
    public static final String CACHE_NAME = "importProgressCache";

    public DefaultBatchProgressCache(Cache importProgressCache) {
        this.importProgressCache = importProgressCache;
    }

    /**
     * 触发异步刷进度
     *
     * @param task 需要被刷进度的 task
     */
    @Override
    public void triggerFlushProgress(ProgressAble task) {
        importProgressCache.put(task.getBatchProgress().getTaskId(), task.getBatchProgress());
        Threads.execute(genFlushProgressTask(task));
    }

    @Override
    public void triggerFlushProgress(BatchProgress batchProgress) {
        importProgressCache.put(batchProgress.getTaskId(), batchProgress);
        Threads.execute(genFlushProgressTask(batchProgress));
    }

    /**
     * 获取任务进度
     *
     * @param id taskId
     * @return 任务
     */
    @Override
    public BatchProgress getTaskProgress(String id) {
        Cache.ValueWrapper valueWrapper = importProgressCache.get(id);
        return valueWrapper == null ? null : (BatchProgress) valueWrapper.get();
    }

    /**
     * 创建一个刷进度的任务
     *
     * @param task 需要被刷进度的任务
     * @return 刷进度的任务
     */
    private Runnable genFlushProgressTask(ProgressAble task) {
        return () -> {
            String id = task.getBatchProgress().getTaskId();
            if (!task.getBatchProgress().hasFinish()) {
                // 未处理完毕，仍需要执行这个任务
                Threads.delay(genFlushProgressTask(task), 1, TimeUnit.SECONDS);
            }
            importProgressCache.put(id, task.getBatchProgress());
        };
    }

    /**
     * 创建一个刷进度的任务
     *
     * @param task 需要被刷进度的任务
     * @return 刷进度的任务
     */
    private Runnable genFlushProgressTask(BatchProgress batchProgress) {
        return () -> {
            String id = batchProgress.getTaskId();
            if (!batchProgress.hasFinish()) {
                // 未处理完毕，仍需要执行这个任务
                Threads.delay(genFlushProgressTask(batchProgress), 1, TimeUnit.SECONDS);
            }
            importProgressCache.put(id, batchProgress);
        };
    }

}
