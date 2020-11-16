package org.shoulder.batch.cache;

import org.shoulder.batch.model.BatchProgress;
import org.shoulder.batch.service.impl.ProgressAble;
import org.shoulder.core.util.Threads;
import org.springframework.cache.Cache;

import java.util.concurrent.TimeUnit;

/**
 * 任务进度存储
 *
 * @author lym
 */
public class ProgressTaskPool {

    // todo 初始化
    private static Cache importProgressCache = null;

    /**
     * 触发异步刷进度
     *
     * @param task 需要被刷进度的 task
     */
    public static void triggerFlushProgress(ProgressAble task) {
        importProgressCache.put(task.getBatchProgress().getTaskId(), task.getBatchProgress());
        Threads.execute(genFlushProgressTask(task));
    }

    /**
     * 获取任务进度
     *
     * @param id taskId
     * @return 任务
     */
    public static BatchProgress getTaskProgress(String id) {
        Cache.ValueWrapper valueWrapper = importProgressCache.get(id);
        return valueWrapper == null ? null : (BatchProgress) valueWrapper.get();
    }

    /**
     * 创建一个刷进度的任务
     *
     * @param task 需要被刷进度的任务
     * @return 刷进度的任务
     */
    private static Runnable genFlushProgressTask(ProgressAble task) {
        return () -> {
            String id = task.getBatchProgress().getTaskId();
            if (!task.getBatchProgress().hasFinish()) {
                // 未处理完毕，仍需要执行这个任务
                Threads.delay(ProgressTaskPool.genFlushProgressTask(task), 1, TimeUnit.SECONDS);
            }
            importProgressCache.put(id, task.getBatchProgress());
        };
    }


}
