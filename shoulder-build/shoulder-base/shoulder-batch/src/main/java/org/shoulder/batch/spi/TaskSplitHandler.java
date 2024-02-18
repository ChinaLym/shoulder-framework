
package org.shoulder.batch.spi;

import jakarta.validation.constraints.NotNull;
import org.shoulder.batch.model.BatchData;
import org.shoulder.batch.model.BatchDataSlice;

import java.util.List;

/**
 * 个性化任务分批
 *
 * @author lym
 */
public interface TaskSplitHandler {

    boolean support(@NotNull BatchData batchData);

    List<BatchDataSlice> splitTask(@NotNull BatchData batchData);

}
