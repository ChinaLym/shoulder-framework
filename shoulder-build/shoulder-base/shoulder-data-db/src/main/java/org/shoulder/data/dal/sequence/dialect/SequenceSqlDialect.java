package org.shoulder.data.dal.sequence.dialect;

public interface SequenceSqlDialect {

    String PLACEHOLDER_CURRENT_TIME = "#CURRENT_TIME#";

    String PLACEHOLDER_TABLE_NAME = "#TABLE_NAME#";

    String PLACEHOLDER_SHARDING_COLUMNS = "#SHARDING_COLUMNS#";

    String PLACEHOLDER_SHARDING_COLUMN_VALUES = "#SHARDING_COLUMN_VALUES#";

    String SQL_PARAMETER_BINDING_CHAR = "?";

    String TEMPLATE_INSERT = "insert into " + PLACEHOLDER_TABLE_NAME + "(name, min_value, max_value, step, value, gmt_create, gmt_modified) values (?, ?, ?, ?, ?, " + PLACEHOLDER_CURRENT_TIME + ", " + PLACEHOLDER_CURRENT_TIME + ")";
    String TEMPLATE_SELECT = "select name, min_value, max_value, step, value, gmt_modified from " + PLACEHOLDER_TABLE_NAME + " where (name = ?)";
    String TEMPLATE_UPDATE = "update " + PLACEHOLDER_TABLE_NAME + " set value=?, gmt_modified = #CURRENT_TIME# where name = ? and value = ?";
    String INSERT_DYNAMIC = "insert into " + PLACEHOLDER_TABLE_NAME + "(" + PLACEHOLDER_SHARDING_COLUMNS + ") values (" + PLACEHOLDER_SHARDING_COLUMN_VALUES + ")";


    String insert();

    String select();

    String update();

    String systemDate();

    default String seqSharding() {
        return INSERT_DYNAMIC;
    }

    String getDbType();
}
