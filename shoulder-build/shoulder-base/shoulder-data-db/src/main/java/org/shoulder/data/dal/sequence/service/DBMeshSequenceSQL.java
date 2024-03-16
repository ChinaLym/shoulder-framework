package org.shoulder.data.dal.sequence.service;

import java.util.List;

/**
 * @author lym
 */
public enum DBMeshSequenceSQL {
    SELECT_EID_SQL(
        "/* ELASTIC_ID= -1 */ SELECT #NAME#.GROUPID as GROUPID, #NAME#.EID AS EID, #NAME#.TABLEID as TABLEID FROM DUAL #SHARDING_CONDITION#"), //
    SELECT_SYS_DATE_SQL("SELECT #NAME#.DBTIMESTAMP AS DBTIMESTAMP FROM DUAL #SHARDING_CONDITION#"), //
    SELECT_VALUE_SQL(
        "SELECT #NAME#.TIMESTAMP AS TIMESTAMP, #NAME#.GROUPID AS GROUPID, #NAME#.EID AS EID, #NAME#.TABLEID AS TABLEID, #NAME#.NEXTVAL AS NEXTVAL FROM DUAL #SHARDING_CONDITION#"); //

    private final String sql;

    DBMeshSequenceSQL(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }

    public String convertToRealSQL(String sequenceName, List<String> shardingCols) {
        String nameReplacedSQL = this.sql.replaceAll("#NAME#", sequenceName);
        StringBuilder sb = new StringBuilder();
        if (shardingCols.size() > 0) {
            sb.append(" WHERE ");
            for (int i = 0; i < shardingCols.size(); i++) {
                if (i > 0) {
                    sb.append(" AND ");
                }

                sb.append(shardingCols.get(i));
                sb.append(" = ?");
            }
        }

        return nameReplacedSQL.replace("#SHARDING_CONDITION#", sb.toString());
    }
}
