package org.shoulder.batch.progress;

import org.shoulder.batch.model.BatchActivityRoot;

/**
 * BatchActivity 存储
 */
public interface BatchActivityRepository {

    void saveActivity(BatchActivityRoot batchActivityRoot, String activityName);

    BatchActivityRoot queryActivity(String activityId);

    /**
     * 运行时注册
     */
    default void register(Class<? extends BatchActivityEnum<?>> enumClass, String displayName) {
        saveActivity(BatchActivityFactory.parseActivity(enumClass), displayName);
    }
}