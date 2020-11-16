package org.shoulder.batch.model.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.shoulder.batch.dto.result.BatchProcessResult;
import org.shoulder.batch.dto.result.BatchRecordDetailResult;
import org.shoulder.batch.dto.result.BatchRecordResult;
import org.shoulder.batch.model.BatchProgress;
import org.shoulder.batch.model.BatchRecord;
import org.shoulder.batch.model.BatchRecordDetail;

/**
 * 对象转换
 *
 * @author lym
 */
@Mapper
public interface BatchModelConvert {

    BatchModelConvert CONVERT = Mappers.getMapper(BatchModelConvert.class);

    @Mapping(expression = "java((int) model.calculateProcessedTime())", target = "timeConsumed")
    @Mapping(expression = "java((int) model.calculateTimeLeft())", target = "timeLeft")
    @Mapping(expression = "java(model.hasFinish())", target = "finish")
    BatchProcessResult toDTO(BatchProgress model);

    @Mapping(source = "creator", target = "operator")
    @Mapping(source = "createTime", target = "executedTime")
    @Mapping(expression = "java(org.apache.commons.collections4.CollectionUtils.emptyIfNull(record.getDetailList()).stream()" +
        ".map(org.shoulder.batch.model.convert.BatchModelConvert.CONVERT::toDTO)" +
        ".collect(java.util.stream.Collectors.toList()))", target = "detailList")
    BatchRecordResult toDTO(BatchRecord record);


    BatchRecordDetailResult toDTO(BatchRecordDetail model);
}
