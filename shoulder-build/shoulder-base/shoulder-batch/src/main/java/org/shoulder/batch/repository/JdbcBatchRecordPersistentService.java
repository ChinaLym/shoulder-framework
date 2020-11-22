package org.shoulder.batch.repository;

import org.shoulder.batch.repository.po.BatchRecordPO;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * 批量处理记录mapper
 *
 * @author lym
 */
public class JdbcBatchRecordPersistentService implements BatchRecordPersistentService {


    private final JdbcTemplate jdbc;

    public JdbcBatchRecordPersistentService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public JdbcBatchRecordPersistentService(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }


    /**
     * 单条插入
     *
     * @param record 批量处理记录
     */
    @Override
    public void insert(BatchRecordPO record) {

    }

    /**
     * 根据 任务标识 获取批处理记录
     *
     * @param importId 主键
     * @return 记录
     */
    @Override
    public BatchRecordPO findById(String importId) {
        return null;
    }

    /**
     * 根据条件分页查询批处理记录
     *
     * @param condition tableName, userCode
     * @return 查询结果
     */
    @Override
    public List<BatchRecordPO> findByPage(Map<String, Object> condition) {
        return null;
    }

    /**
     * 根据用户编码查询最近批处理的记录
     *
     * @param condition tableName, userCode, flag
     * @return 上次批量处理记录
     */
    @Override
    public BatchRecordPO findLast(Map<String, Object> condition) {
        return null;
    }

}
