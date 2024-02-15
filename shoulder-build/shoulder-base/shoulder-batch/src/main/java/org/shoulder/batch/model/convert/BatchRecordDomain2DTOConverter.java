
package org.shoulder.batch.model.convert;

import jakarta.annotation.Nonnull;
import org.shoulder.batch.dto.result.BatchRecordDetailResult;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.core.i18.Translator;

/**
 * 批处理记录 core -> DTO
 *
 * @author lym
 */
public class BatchRecordDomain2DTOConverter extends BaseDataConverter<BatchRecord, BatchRecordResult> {

    public static BatchRecordDomain2DTOConverter INSTANCE = new BatchRecordDomain2DTOConverter(null);

    private final Translator translator;

    public BatchRecordDomain2DTOConverter(Translator translator) {
        this.translator = translator;
    }

    @Override
    public void doConvert(@Nonnull BatchRecord sourceModel, @Nonnull BatchRecordResult targetModel) {
        targetModel.setTotalNum(sourceModel.getTotalNum());
        targetModel.setSuccessNum(sourceModel.getSuccessNum());
        targetModel.setFailNum(sourceModel.getFailNum());
        targetModel.setDataType(sourceModel.getDataType());

        targetModel.setOperation(sourceModel.getOperation());
        targetModel.setOperator(sourceModel.getCreator());
        targetModel.setExecutedTime(sourceModel.getCreateTime());
        targetModel.setDetailList(conversionService.convert(sourceModel.getDetailList(), BatchRecordDetailResult.class));
    }

}
