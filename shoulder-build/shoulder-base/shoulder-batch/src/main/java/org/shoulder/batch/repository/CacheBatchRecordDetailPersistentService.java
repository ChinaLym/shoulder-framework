package org.shoulder.batch.repository;

import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.batch.enums.BatchResultEnum;
import org.shoulder.batch.model.BatchRecordDetail;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 批处理记录持久化接口
 *
 * @author lym
 */
public class CacheBatchRecordDetailPersistentService implements BatchRecordDetailPersistentService {

    /**
     * 缓存
     */
    private final Cache cache;

    public CacheBatchRecordDetailPersistentService(Cache cache) {
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

    /**
     * 查询所有的批量处理记录
     *
     * @param recordId   记录标识
     * @param resultList 结果状态
     * @return 所有的批量处理记录
     */
    @Override
    public List<BatchRecordDetail> findAllByResult(String recordId, List<BatchResultEnum> resultList) {

        return CollectionUtils.emptyIfNull(findAllByResult(recordId)).stream()
            .filter(detail -> resultList.contains(BatchResultEnum.of(detail.getStatus())))
            .collect(Collectors.toList());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<BatchRecordDetail> findAllByResult(String recordId) {
        Cache.ValueWrapper wrapper = cache.get(recordId);
        return wrapper == null ? null : (List<BatchRecordDetail>) wrapper.get();
    }


}
