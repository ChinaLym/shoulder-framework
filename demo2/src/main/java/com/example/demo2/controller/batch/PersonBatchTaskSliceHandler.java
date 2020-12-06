package com.example.demo2.controller.batch;

import com.example.demo2.dto.PersonRecord;
import lombok.SneakyThrows;
import org.shoulder.batch.enums.BatchResultEnum;
import org.shoulder.batch.model.BatchDataSlice;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.service.ext.BatchTaskSliceHandler;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 处理人员校验
 *
 * @author lym
 */
@Component
public class PersonBatchTaskSliceHandler implements BatchTaskSliceHandler {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public List<BatchRecordDetail> handle(BatchDataSlice task) {
        List<PersonRecord> dataList = (List<PersonRecord>) task.getBatchList();
        List<BatchRecordDetail> processResult = new ArrayList<>(dataList.size());
        for (PersonRecord personRecord : dataList) {
            log.info("processing the no.{} data({}), ", personRecord.getRowNum(), personRecord);

            // 模拟校验比较耗时，校验字段、业务校验、查数据库、调接口等...
            Thread.sleep(1000);

            // 设置每条处理结果信息
            BatchRecordDetail result = new BatchRecordDetail();
            result.setId(idGenerator.getAndIncrement())
                    .setOperation(task.getOperationType())
                    .setRecordId(UUID.randomUUID().toString())
                    .setSource(JsonUtils.toJson(personRecord))
                    .setRowNum(personRecord.getRowNum())
                    .setStatus(BatchResultEnum.VALIDATE_SUCCESS.getCode());

            if ((result.getId() % 3) == 0) {
                result.setFailReason("随机失败几个");
                result.setStatus(BatchResultEnum.VALIDATE_FAILED.getCode());
            }

            processResult.add(result);
        }
        return processResult;
    }

    @Override
    public boolean support(String dataType, String operationType) {
        return DemoBatchConstants.DATA_TYPE_PERSON.equals(dataType)
                && DemoBatchConstants.OPERATION_VALIDATE.equals(operationType);
    }
}
