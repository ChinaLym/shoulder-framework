package org.shoulder.data.dal.sequence.generator;

import org.shoulder.core.util.StringUtils;

/**
 * @author lym
 */
public enum SequenceSQLStatementType {

    INSERT_SQL("insertSql"), //
    SEQ_SHARDING_SQL("seqShardingSql"), //
    SYS_DATE_SQL("systemDateSql"), //
    UPDATE_SQL("updateSql"), //
    SELECT_SQL("selectSql");

    private String name;

    private SequenceSQLStatementType(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    /**
     * Convert String into an enum type
     *
     * @param name
     * @return
     */
    public static SequenceSQLStatementType convert(String name) {
        if (StringUtils.isEmpty(name))
            return null;
        if (INSERT_SQL.toString().equalsIgnoreCase(name)) {
            return INSERT_SQL;
        }
        if (SEQ_SHARDING_SQL.toString().equalsIgnoreCase(name)) {
            return SEQ_SHARDING_SQL;
        }
        if (SYS_DATE_SQL.toString().equalsIgnoreCase(name)) {
            return SYS_DATE_SQL;
        }
        if (UPDATE_SQL.toString().equalsIgnoreCase(name)) {
            return UPDATE_SQL;
        }
        if (SELECT_SQL.toString().equalsIgnoreCase(name)) {//5
            return SELECT_SQL;
        }
        return null;
    }
}
