
package org.shoulder.batch.model.convert;

import jakarta.annotation.Nonnull;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.progress.BatchProgressRecord;
import org.shoulder.core.converter.BaseDataConverter;

/**
 * 进度条 core -> DTO
 *
 * @author lym
 */
public class BatchProgressRecordDomain2DTOConverter extends BaseDataConverter<BatchProgressRecord, BatchProcessResult> {

    public static BatchProgressRecordDomain2DTOConverter INSTANCE = new BatchProgressRecordDomain2DTOConverter();

    @Override
    public void doConvert(@Nonnull BatchProgressRecord sourceModel, @Nonnull BatchProcessResult targetModel) {

        targetModel.setTotalNum(sourceModel.getTotal());
        targetModel.setFailNum(sourceModel.getFailNum());
        targetModel.setSuccessNum(sourceModel.getSuccessNum());
        targetModel.setProcessed(sourceModel.getProcessed());
        targetModel.setTimeConsumed(sourceModel.calculateProcessedTime());
        targetModel.setTimeLeft(sourceModel.calculateTimeLeft());
        targetModel.setStatus(sourceModel.getStatus());
    }
}
