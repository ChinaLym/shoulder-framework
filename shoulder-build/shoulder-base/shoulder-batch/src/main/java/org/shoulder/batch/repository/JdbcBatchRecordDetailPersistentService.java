package org.shoulder.batch.repository;

import org.shoulder.batch.repository.po.BatchRecordDetailPO;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * 批处理记录持久化接口
 *
 * @author lym
 */
public class JdbcBatchRecordDetailPersistentService implements BatchRecordDetailPersistentService {


    private final JdbcTemplate jdbc;

    public JdbcBatchRecordDetailPersistentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public JdbcBatchRecordDetailPersistentService(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    /**
     * 批量新增处理详情
     *
     * @param batchRecordDetailList 要插入的记录
     */
    @Override
    public void batchInsertRecordDetail(List<BatchRecordDetailPO> batchRecordDetailList) {

    }

    /**
     * 查询所有的批量处理记录
     *
     * @param recordId   记录标识
     * @param resultList 结果状态
     * @return 所有的批量处理记录
     */
    @Override
    public List<BatchRecordDetailPO> findAllByResult(String recordId, List<Integer> resultList) {
        return null;
    }


}
