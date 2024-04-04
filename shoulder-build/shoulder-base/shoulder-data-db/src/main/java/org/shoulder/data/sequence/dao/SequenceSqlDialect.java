package org.shoulder.data.sequence.dao;

public interface SequenceSqlDialect {

    String PLACEHOLDER_CURRENT_TIME = "#CURRENT_TIME#";

    String PLACEHOLDER_TABLE_NAME = "#TABLE_NAME#";

    String PLACEHOLDER_DYNAMIC_COLUMNS = "#DYNAMIC_COLUMNS#";

    String PLACEHOLDER_DYNAMIC_COLUMN_VALUES = "#DYNAMIC_COLUMN_VALUES#";

    String SQL_PARAMETER_BINDING_CHAR = "?";

    String TEMPLATE_INSERT = "INSERT INTO " + PLACEHOLDER_TABLE_NAME + "(name, min_value, max_value, step, current_value, create_time, update_time) VALUES (?, ?, ?, ?, ?, " + PLACEHOLDER_CURRENT_TIME + ", " + PLACEHOLDER_CURRENT_TIME + ")";
    String TEMPLATE_SELECT = "SELECT name, min_value, max_value, step, current_value, update_time FROM " + PLACEHOLDER_TABLE_NAME + " WHERE (name = ?)";
    String TEMPLATE_UPDATE = "UPDATE " + PLACEHOLDER_TABLE_NAME + " SET current_value=?, update_time = #CURRENT_TIME# WHERE name = ? AND current_value = ?";
    String INSERT_DYNAMIC = "INSERT INTO " + PLACEHOLDER_TABLE_NAME + "(" + PLACEHOLDER_DYNAMIC_COLUMNS + ") VALUES (" + PLACEHOLDER_DYNAMIC_COLUMN_VALUES
                            + ")";


    String insert();

    String select();

    String update();

    String systemDate();

    default String insertDynamic() {
        return INSERT_DYNAMIC;
    }

    String getDbType();
}
