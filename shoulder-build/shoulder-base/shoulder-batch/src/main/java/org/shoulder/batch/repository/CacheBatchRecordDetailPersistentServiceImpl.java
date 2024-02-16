package org.shoulder.batch.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.enums.ProcessStatusEnum;
import org.shoulder.batch.model.BatchRecordDetail;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 批处理记录持久化接口
 *
 * @author lym
 */
public class CacheBatchRecordDetailPersistentServiceImpl implements BatchRecordDetailPersistentService {

    /**
     * 缓存
     */
    private final Cache cache;

    public CacheBatchRecordDetailPersistentServiceImpl(Cache cache) {
        this.cache = cache;
    }

    /**
     * 批量新增处理详情
     *
     * @param batchRecordDetailList 要插入的记录
     */
    @Override
    public void batchSave(String recordId, List<BatchRecordDetail> batchRecordDetailList) {
        cache.put(recordId, batchRecordDetailList);
    }

    @Override public List<BatchRecordDetail> findAllByRecordIdAndStatusAndIndex(String recordId, List<ProcessStatusEnum> resultList, Integer indexStart,
                                                                                Integer indexEnd) {
        return CollectionUtils.emptyIfNull(findAllByRecordId(recordId)).stream()
            .filter(detail -> resultList.contains(ProcessStatusEnum.of(detail.getStatus())))
            .filter(detail -> indexStart == null || detail.getIndex() >= indexStart)
            .filter(detail -> indexEnd == null || detail.getIndex() <= indexEnd)
            .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BatchRecordDetail> findAllByRecordId(String recordId) {
        Cache.ValueWrapper wrapper = cache.get(recordId);
        return wrapper == null ? null : (List<BatchRecordDetail>) wrapper.get();
    }

}
