
package org.shoulder.batch.model.convert;

import jakarta.annotation.Nonnull;
import org.shoulder.batch.dto.result.BatchRecordDetailResult;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.core.i18.Translator;

/**
 * BatchRecord domain -> VO
 *
 * @author lym
 */
public class BatchProcessRecordDomain2DTOConverter extends BaseDataConverter<BatchRecord, BatchRecordResult> {

    public static BatchProcessRecordDomain2DTOConverter INSTANCE = new BatchProcessRecordDomain2DTOConverter(null);

    private final Translator translator;

    public BatchProcessRecordDomain2DTOConverter(Translator translator) {
        this.translator = translator;
    }

    @Override
    public void doConvert(@Nonnull BatchRecord sourceModel, @Nonnull BatchRecordResult targetModel) {

        targetModel.setTotalNum(sourceModel.getTotalNum());
        targetModel.setFailNum(sourceModel.getFailNum());
        targetModel.setSuccessNum(sourceModel.getSuccessNum());
        targetModel.setDataType(sourceModel.getDataType());
        targetModel.setOperation(sourceModel.getOperation());
        targetModel.setOperator(sourceModel.getCreator());
        targetModel.setExecutedTime(sourceModel.getCreateTime());
        targetModel.setDetailList(conversionService.convert(sourceModel.getDetailList(), BatchRecordDetailResult.class));
    }
}
