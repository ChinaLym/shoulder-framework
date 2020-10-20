package org.shoulder.crypto.local.repository.impl;

import org.shoulder.crypto.local.entity.LocalCryptoInfoEntity;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

/**
 * 使用 数据库 作为存储
 *
 * @author lym
 */
public class JdbcLocalCryptoInfoRepository implements LocalCryptoInfoRepository {

    private static final String CREATE_STATEMENT =
        "create table crypt_info(" +
            "    component_id  VARCHAR(32) NOT NULL COMMENT '应用标识'," +
            "    header        VARCHAR(32) NOT NULL default '' COMMENT '密文前缀标识，算法标识'," +
            "    data_key      VARCHAR(64) NOT NULL COMMENT '数据密钥（密文）'," +
            "    root_key_part VARCHAR(64) COMMENT '根密钥部件'," +
            "    vector        VARCHAR(64) COMMENT '初始偏移向量'," +
            "    create_time   DATETIME             default now() COMMENT '创建时间'," +
            "    PRIMARY KEY pk_crypt_info (component_id, header)" +
            ") ENGINE = INNODB" +
            "  DEFAULT CHARSET = UTF8MB4 COMMENT = '加密元信息';";

    private static final String FIELDS = "app_id, header, data_key, root_key_part, vector, create_time";

    private static final String TABLE_NAME = "crypt_info";

    protected static final String SELECT_STATEMENT = "SELECT " + FIELDS + " FROM " + TABLE_NAME;

    protected static final String DEFAULT_INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" + FIELDS
        + ") values (?,?,?,?,?,?)";

    private static final String WHERE = " WHERE app_id = ? ";

    protected static final String SELECT_BATCH_STATEMENT = SELECT_STATEMENT + WHERE;

    private static final String WHERE_UNIQUE = WHERE + " AND header = ? ";

    protected static final String SELECT_SINGLE_STATEMENT = SELECT_STATEMENT + WHERE_UNIQUE;

    private JdbcTemplate jdbcTemplate;

    private RowMapper<LocalCryptoInfoEntity> rowMapper = new AesInfoRowMapper();

    private String selectBatchSql = SELECT_BATCH_STATEMENT;

    private String selectSingleSql = SELECT_SINGLE_STATEMENT;

    private String insertSql = DEFAULT_INSERT_STATEMENT;

    public JdbcLocalCryptoInfoRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setRowMapper(RowMapper<LocalCryptoInfoEntity> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public void setSelectBatchSql(String selectBatchSql) {
        this.selectBatchSql = selectBatchSql;
    }

    public void setSelectSingleSql(String selectSingleSql) {
        this.selectSingleSql = selectSingleSql;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    @Override
    public void save(@NonNull LocalCryptoInfoEntity aesInfo) throws Exception {
        // 使用唯一索引保证一致性
        jdbcTemplate.update(insertSql, getAllFields(aesInfo));
    }

    @Override
    public LocalCryptoInfoEntity get(String appId, String markHeader) throws Exception {
        try {
            Object[] whereFields = new Object[]{appId, markHeader};
            return jdbcTemplate.queryForObject(selectSingleSql, rowMapper, whereFields);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    @Override
    @NonNull
    public List<LocalCryptoInfoEntity> get(String appId) {
        try {
            Object[] whereFields = new Object[]{appId};
            return jdbcTemplate.query(selectBatchSql, rowMapper, whereFields);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    protected Object[] getAllFields(LocalCryptoInfoEntity aesInfo) {
        return new Object[]{
            aesInfo.getAppId(),
            aesInfo.getHeader(),
            aesInfo.getDataKey(),
            aesInfo.getRootKeyPart(),
            aesInfo.getVector(),
            aesInfo.getCreateTime(),
        };
    }

    protected Object[] getWhereFields(LocalCryptoInfoEntity aesInfo) {
        return new Object[]{
            aesInfo.getAppId(),
            aesInfo.getHeader()
        };
    }

    private static class AesInfoRowMapper implements RowMapper<LocalCryptoInfoEntity> {

        @Override
        public LocalCryptoInfoEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            LocalCryptoInfoEntity entity = new LocalCryptoInfoEntity();
            entity.setAppId(rs.getString(0));
            entity.setHeader(rs.getString(1));
            entity.setDataKey(rs.getString(2));
            entity.setRootKeyPart(rs.getString(3));
            entity.setVector(rs.getString(4));
            entity.setCreateTime(rs.getDate(5));
            return entity;
        }
    }

}
