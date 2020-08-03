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

    private JdbcTemplate jdbcTemplate;

    private RowMapper<LocalCryptoInfoEntity> rowMapper = new AesInfoRowMapper();

    private static final String CREATE_STATEMENT =
        "create table tb_security_info " +
            "( " +
            "     id varchar " +
            "     app_id varchar, " +
            "     data_key varchar, " +
            "     root_key_part varchar, " +
            "     iv varchar, " +
            "     header varchar, " +
            "     create_time date, " +

            "     constraint tb_security_info_pk " +
            "          primary key, " +
            "     constraint tb_security_info_uk " +
            "          unique (app_id, header) " +
            "); " +
            " " +
            "comment on table tb_security_info is 'support local crypto';";

    private static final String FIELDS = "id, app_id, data_key, root_key_part, iv, header, create_time";

    private static final String TABLE_NAME = "tb_security_info";

    private static final String WHERE = " WHERE app_id = ? ";

    private static final String WHERE_UNIQUE = WHERE + " AND header = ? ";

    protected static final String SELECT_STATEMENT = "SELECT " + FIELDS + " FROM " + TABLE_NAME;

    protected static final String SELECT_SINGLE_STATEMENT = SELECT_STATEMENT + WHERE_UNIQUE;

    protected static final String SELECT_BATCH_STATEMENT = SELECT_STATEMENT + WHERE;

    protected static final String DEFAULT_INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " (" + FIELDS
        + ") values (?,?,?,?,?,?,?)";


    private String selectBatchSql = SELECT_BATCH_STATEMENT;

    private String selectSingleSql = SELECT_SINGLE_STATEMENT;

    private String insertSql = DEFAULT_INSERT_STATEMENT;

    public JdbcLocalCryptoInfoRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
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
            aesInfo.getId(),
            aesInfo.getAppId(),
            aesInfo.getDataKey(),
            aesInfo.getRootKeyPart(),
            aesInfo.getIv(),
            aesInfo.getHeader(),
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
            return new LocalCryptoInfoEntity(
                rs.getString(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4),
                rs.getString(5),
                rs.getString(6),
                rs.getDate(7)
            );
        }
    }

}
