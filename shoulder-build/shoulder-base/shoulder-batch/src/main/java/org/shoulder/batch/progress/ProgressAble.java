package org.shoulder.batch.progress;

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
     * 结束后的回调
     */
    default void onFinished(String id, ProgressAble task) {


    }
}
