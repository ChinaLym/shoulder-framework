package org.shoulder.batch.model;

import lombok.Data;
import org.shoulder.batch.progress.BatchActivityEnum;

import java.util.List;

@Data
public class BatchActivityRoot {

    private String id;

    private String displayName;

    private String endStepId;

    private List<BatchActivityBlock> activityBlocks;

    private transient Class<? extends BatchActivityEnum<?>> originalClass;
}