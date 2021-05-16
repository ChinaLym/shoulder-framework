package org.shoulder.crypto.local.repository.impl;

import org.shoulder.crypto.local.entity.LocalCryptoMetaInfo;
import org.shoulder.crypto.local.repository.LocalCryptoInfoRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.annotation.Nonnull;
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

    /**
     * todo 【SQL】使用 resource Loader 加载 ddl
     */
    private static final String CREATE_STATEMENT =
            "create table crypto_info(" +
                    "    app_id        VARCHAR(32) NOT NULL COMMENT '应用标识'," +
                    "    header        VARCHAR(32) NOT NULL default '' COMMENT '密文前缀标识，算法标识'," +
                    "    data_key      VARCHAR(64) NOT NULL COMMENT '数据密钥（密文）'," +
                    "    root_key_part VARCHAR(64) COMMENT '根密钥部件'," +
                    "    vector        VARCHAR(64) COMMENT '初始偏移向量'," +
                    "    create_time   DATETIME             default now() COMMENT '创建时间'," +
                    "    PRIMARY KEY pk_crypto_info (app_id, header)" +
                    ") ENGINE = INNODB" +
                    "  DEFAULT CHARSET = UTF8MB4 COMMENT = '加密元信息';";

    private static final String FIELDS = "app_id, header, data_key, root_key_part, vector, create_time";

    private static final String TABLE_NAME = "crypto_info";

    protected static final String SELECT_STATEMENT = "SELECT " + FIELDS + " FROM " + TABLE_NAME;

    protected static final String DEFAULT_INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" + FIELDS
        + ") values (?,?,?,?,?,?)";

    private static final String WHERE = " WHERE app_id = ? ";

    protected static final String SELECT_BATCH_STATEMENT = SELECT_STATEMENT + WHERE;

    private static final String WHERE_UNIQUE = WHERE + " AND header = ? ";

    protected static final String SELECT_SINGLE_STATEMENT = SELECT_STATEMENT + WHERE_UNIQUE;

    private JdbcTemplate jdbcTemplate;

    private RowMapper<LocalCryptoMetaInfo> rowMapper = new AesInfoRowMapper();

    private String selectBatchSql = SELECT_BATCH_STATEMENT;

    private String selectSingleSql = SELECT_SINGLE_STATEMENT;

    private String insertSql = DEFAULT_INSERT_STATEMENT;

    public JdbcLocalCryptoInfoRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setRowMapper(RowMapper<LocalCryptoMetaInfo> rowMapper) {
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
    public void save(@Nonnull LocalCryptoMetaInfo localCryptoMetaInfo) {
        // 使用唯一索引保证一致性
        jdbcTemplate.update(insertSql, getAllFields(localCryptoMetaInfo));
    }

    @Override
    public LocalCryptoMetaInfo get(@Nonnull String appId, @Nonnull String markHeader) {
        try {
            Object[] whereFields = new Object[]{appId, markHeader};
            return jdbcTemplate.queryForObject(selectSingleSql, rowMapper, whereFields);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }


    @Override
    @Nonnull
    public List<LocalCryptoMetaInfo> get(@Nonnull String appId) {
        try {
            Object[] whereFields = new Object[]{appId};
            return jdbcTemplate.query(selectBatchSql, rowMapper, whereFields);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    protected Object[] getAllFields(LocalCryptoMetaInfo aesInfo) {
        return new Object[]{
            aesInfo.getAppId(),
            aesInfo.getHeader(),
            aesInfo.getDataKey(),
            aesInfo.getRootKeyPart(),
            aesInfo.getVector(),
            aesInfo.getCreateTime(),
        };
    }

    protected Object[] getWhereFields(LocalCryptoMetaInfo aesInfo) {
        return new Object[]{
            aesInfo.getAppId(),
            aesInfo.getHeader()
        };
    }

    private static class AesInfoRowMapper implements RowMapper<LocalCryptoMetaInfo> {

        @Override
        public LocalCryptoMetaInfo mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            LocalCryptoMetaInfo entity = new LocalCryptoMetaInfo();
            // 第一个下标为 1
            entity.setAppId(rs.getString(1));
            entity.setHeader(rs.getString(2));
            entity.setDataKey(rs.getString(3));
            entity.setRootKeyPart(rs.getString(4));
            entity.setVector(rs.getString(5));
            entity.setCreateTime(rs.getDate(6));
            return entity;
        }
    }

    /**
     * 支持集群
     */
    @Override
    public boolean supportCluster() {
        return true;
    }

}
