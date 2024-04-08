
package org.shoulder.batch.spi;

import org.shoulder.batch.enums.BatchDetailResultStatusEnum;
import org.shoulder.batch.log.ShoulderBatchLoggers;
import org.shoulder.batch.model.BatchDataSlice;
import org.shoulder.batch.model.BatchRecordDetail;
import org.shoulder.batch.repository.BatchRecordDetailPersistentService;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.util.AssertUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 通用数据导入处理器
 *
 * @author lym
 */
public abstract class BaseImportHandler implements BatchTaskSliceHandler {

    protected final Logger logger = ShoulderBatchLoggers.DEFAULT;

    private final String dataType;
    private final String operationType;

    private final BatchRecordDetailPersistentService batchRecordDetailPersistentService;

    public BaseImportHandler(String dataType, String operationType,
                             BatchRecordDetailPersistentService batchRecordDetailPersistentService) {
        this.dataType = dataType;
        this.operationType = operationType;
        this.batchRecordDetailPersistentService = batchRecordDetailPersistentService;
    }

    @Override public boolean support(String dataType, String operationType) {
        return this.dataType.equals(dataType)
               && this.operationType.equals(operationType);
    }

    @Override public List<BatchRecordDetail> handle(BatchDataSlice task) {

        // 分批参数
        BatchImportDataItem batchImportDataItem = ImportTaskSplitHandler.fetchBatchImportDataItem(task.getBatchList());
        int limit = batchImportDataItem.getBatchSliceSize();
        int start = task.getSequence() * limit;
        int end = start + limit - 1;

        // 查询本批次所有数据
        List<BatchRecordDetail> batchRecordDetails = batchRecordDetailPersistentService.findAllByRecordIdAndStatusAndIndex(
            batchImportDataItem.getSourceBatchId(), null, start, end);

        // 确认执行参数
        boolean updateRepeat = batchImportDataItem.getExtAttribute(BatchImportDataItem.EXT_KEY_UPDATE_REPEAT);

        // 按校验结果分类
        List<BatchRecordDetail> toImportList = new ArrayList<>();
        List<BatchRecordDetail> toUpdateList = new ArrayList<>();
        List<BatchRecordDetail> ignoreList = new ArrayList<>();
        for (BatchRecordDetail batchRecordDetail : batchRecordDetails) {
            BatchDetailResultStatusEnum status = BatchDetailResultStatusEnum.of(batchRecordDetail.getStatus());
            List<BatchRecordDetail> list = switch (status) {
                case SUCCESS -> toImportList;
                case FAILED -> updateRepeat ? toUpdateList : ignoreList;
                case FAILED_FOR_INVALID, FAILED_FOR_REPEAT -> ignoreList;
                default -> throw new BaseRuntimeException("unexpected status: " + batchRecordDetail.getStatus());
            };
            list.add(batchRecordDetail);
        }

        // 导入数据
        List<BatchRecordDetail> importResultList = saveData(toImportList);

        // 更新重复数据
        List<BatchRecordDetail> existResultList = updateData(toUpdateList);

        // 跳过数据
        List<BatchRecordDetail> ignoreResultList = ignoreList.stream().map(this::ignoreData).toList();

        // 聚合结果
        List<BatchRecordDetail> allResultList = new ArrayList<>(limit);
        allResultList.addAll(importResultList);
        allResultList.addAll(existResultList);
        allResultList.addAll(ignoreResultList);
        boolean notAllSetSourceStr = allResultList.stream()
            .map(BatchRecordDetail::getSource)
            .anyMatch(Objects::isNull);
        AssertUtils.isFalse(notAllSetSourceStr, CommonErrorCodeEnum.CODING, "impl need invoke setSource().");

        allResultList.sort(Comparator.comparingInt(BatchRecordDetail::getIndex));

        task.setBatchList(batchRecordDetails);

        return allResultList;
    }

    protected BatchRecordDetail ignoreData(BatchRecordDetail ignore) {
        BatchDetailResultStatusEnum translatedStatus = switch (BatchDetailResultStatusEnum.of(ignore.getStatus())) {
            case FAILED, FAILED_FOR_INVALID, SKIP_FOR_INVALID -> BatchDetailResultStatusEnum.SKIP_FOR_INVALID;
            case FAILED_FOR_REPEAT, SKIP_FOR_REPEAT -> BatchDetailResultStatusEnum.SKIP_FOR_REPEAT;
            default -> throw new BaseRuntimeException("unexpected status: " + ignore.getStatus());
        };
        return BatchRecordDetail.builder()
            .index(ignore.getIndex())
            .status(translatedStatus.getCode())
            .source(ignore.getSource())
            .build();
    }

    protected abstract List<BatchRecordDetail> updateData(List<BatchRecordDetail> toUpdateList);

    protected abstract List<BatchRecordDetail> saveData(List<BatchRecordDetail> toImportList);

}
