
package org.shoulder.batch.model.convert;

import jakarta.annotation.Nonnull;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.progress.BatchProgressRecord;
import org.shoulder.batch.progress.Progress;
import org.shoulder.batch.progress.ProgressStatusEnum;
import org.shoulder.core.converter.BaseDataConverter;

/**
 * 进度条 core -> DTO
 *
 * @author lym
 */
public class BatchProgressRecordDomain2DTOConverter extends BaseDataConverter<Progress, BatchProcessResult> {

    public static BatchProgressRecordDomain2DTOConverter INSTANCE = new BatchProgressRecordDomain2DTOConverter();

    @Override
    public void doConvert(@Nonnull Progress sourceModel, @Nonnull BatchProcessResult targetModel) {

        BatchProgressRecord record = sourceModel.toProgressRecord();

        targetModel.setTotalNum(record.getTotal());
        targetModel.setFailNum(record.getFailNum());
        targetModel.setSuccessNum(record.getSuccessNum());
        targetModel.setProcessed(record.getProcessed());
        targetModel.setFinish(record.hasFinish());
        targetModel.setTimeConsumed(record.calculateProcessedTime());
        targetModel.setTimeLeft(record.calculateTimeLeft());
        targetModel.setStatus(record.getStatus());
        int s = record.getStatus();
        for (ProgressStatusEnum statusEnum : ProgressStatusEnum.values()) {
            if(statusEnum.ordinal() == s) {
                targetModel.setStatusInfo(statusEnum.name());
            }
        }
    }
}
