package org.shoulder.data.sequence.dao;

import java.util.Optional;

/**
 * 默认的 sequence sql 方言
 *
 * @author lym
 */
public enum DefaultSequenceSqlDialectEnum implements SequenceSqlDialect {


    MYSQL("MYSQL",
        TEMPLATE_INSERT.replace(PLACEHOLDER_CURRENT_TIME, "current_timestamp()"),
        TEMPLATE_SELECT,
        TEMPLATE_UPDATE.replace(PLACEHOLDER_CURRENT_TIME, "current_timestamp()"),
        "select current_timestamp()",
        INSERT_DYNAMIC
    ),
    ORACLE("ORACLE",
        TEMPLATE_INSERT.replace(PLACEHOLDER_CURRENT_TIME, "sysdate"),
        TEMPLATE_SELECT,
        TEMPLATE_UPDATE.replace(PLACEHOLDER_CURRENT_TIME, "sysdate"),
        "select current_timestamp as realdate from dual",
        INSERT_DYNAMIC
    );


    private final String insertSql;
    private final String selectSql;
    private final String updateSql;
    private final String systemDateSql;
    private final String seqShardingSql;
    private final String dbType;

    DefaultSequenceSqlDialectEnum(String dbType, String insertSql, String selectSql, String updateSql, String systemDateSql, String seqShardingSql) {
        this.dbType = dbType;
        this.insertSql = insertSql;
        this.selectSql = selectSql;
        this.updateSql = updateSql;
        this.systemDateSql = systemDateSql;
        this.seqShardingSql = Optional.ofNullable(seqShardingSql).orElse(INSERT_DYNAMIC);
    }

    @Override
    public String insert() {
        return insertSql;
    }

    @Override
    public String select() {
        return selectSql;
    }

    @Override
    public String update() {
        return updateSql;
    }

    @Override
    public String systemDate() {
        return systemDateSql;
    }

    @Override
    public String insertDynamic() {
        return seqShardingSql;
    }

    @Override
    public String getDbType() {
        return dbType;
    }
}
