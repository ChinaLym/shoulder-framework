package org.shoulder.batch.progress;

import org.shoulder.batch.model.BatchActivityRoot;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认 BatchActivity 存储
 *
 * @author lym
 */
public class DefaultBatchActivityRepository implements BatchActivityRepository {

    public final ConcurrentHashMap<String, BatchActivityRoot> repository = new ConcurrentHashMap<>();

    public void saveActivity(BatchActivityRoot batchActivityRoot, String activityName) {
        batchActivityRoot.setDisplayName(activityName);
        repository.put(batchActivityRoot.getId(), batchActivityRoot);
    }

    public BatchActivityRoot queryActivity(String activityId) {
        return repository.get(activityId);
    }

}