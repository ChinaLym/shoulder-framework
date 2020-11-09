package org.shoulder.batch.service;

import org.shoulder.core.util.SpringUtils;
import org.shoulder.core.util.Threads;

import java.util.concurrent.TimeUnit;

public class ProgressTaskPool {


    public static Cache importProgressCache = SpringUtils.getCache("batch:importProcess");

    /**
     * 触发异步刷进度
     *
     * @param task 需要被刷进度的 task
     */
    public static void triggerFlushProgressAsync(ImportRunnable task) {
        importProgressCache.put(task.getTaskId(), task.currentProgressAndResult());
        Threads.execute(genFlushProgressTask(task));
    }

    /**
     * 获取任务进度
     *
     * @param id taskId
     * @return 任务
     */
    public static ImportResult getTaskProgress(String id) {
        return importProgressCache.get(id);
    }


    /**
     * 创建一个刷进度的任务
     *
     * @param task 需要被刷进度的导入 task
     * @return 刷进度的任务
     */
    static Runnable genFlushProgressTask(ImportRunnable task) {
        return () -> {
            String id = task.getTaskId();
            if (!task.finished) {
                // 未导入完毕，仍需要执行这个任务
                Threads.delay(ProgressTaskPool.genFlushProgressTask(task), 1, TimeUnit.SECONDS);
            }
            importProgressCache.put(id, task.currentProgressAndResult());
        };
    }


}
