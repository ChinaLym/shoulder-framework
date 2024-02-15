
package org.shoulder.batch.model.convert;

import jakarta.annotation.Nonnull;
import org.shoulder.batch.dto.result.BatchRecordDetailResult;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.core.converter.BaseDataConverter;
import org.shoulder.core.i18.Translator;

import java.util.Collections;

/**
 * 处理详情 core -> DTO
 *
 * @author lym
 */
public class BatchRecordDetailDomain2DTOConverter extends BaseDataConverter<BatchRecordDetail, BatchRecordDetailResult> {

    public static BatchRecordDetailDomain2DTOConverter INSTANCE = new BatchRecordDetailDomain2DTOConverter(null);

    private final Translator translator;

    public BatchRecordDetailDomain2DTOConverter(Translator translator) {
        this.translator = translator;
    }

    @Override
    public void doConvert(@Nonnull BatchRecordDetail sourceModel, @Nonnull BatchRecordDetailResult targetModel) {
        targetModel.setIndex(sourceModel.getIndex());
        targetModel.setStatus(sourceModel.getStatus());
        targetModel.setReason(translator.getMessage(sourceModel.getFailReason()));
        targetModel.setSource(sourceModel.getSource());

        // todo 用于填充翻译项
        targetModel.setReasonParam(Collections.emptyList());
    }
}
