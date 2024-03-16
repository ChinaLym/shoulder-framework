package org.shoulder.data.dal.sequence;

public class ThreadLocalString {

    public static final String ROUTE_CONDITION = "ROUTE_CONDITION",
        IS_EXIST_QUITE = "IS_EXIST_QUITE";

    @Deprecated
    // this is  an internal interface so that user is forbidden to use it
    public static final String SEQUENCE_ROUTE_CONDITION = "SEQUENCE_ROUTE_CONDITION";

    public static final String PARTITION_CONDITION = "PARTITION_CONDITION";

    public static final String TABLE_MERGE_SORT_TABLENAME = "TABLE_MERGE_SORT_TABLENAME",
        TABLE_MERGE_SORT_POOL = "TABLE_MERGE_SORT_POOL",
        TABLE_MERGE_SORT_VIRTUAL_TABLE_NAME = "TABLE_MERGE_SORT_VIRTUAL_TABLE_NAME";
    /**
     * added by fanzeng,即选择某个读库来执行某条sql
     */
    public static final String DATABASE_INDEX = "DATABASE_INDEX";

    /**
     * 在指定库读时，只选择写库，不进行failover.
     */
    public static final String WRITE_DATABASE_READ_RETRY = "WRITE_DATABASE_READ_RETRY";
    /**
     * added by fanzeng,以支持cif提出的需求，即选择读库还是写库来执行某条sql
     */
    public static final String SELECT_DATABASE = "SELECT_DATABASE";
    /**
     * added by fanzeng, 支持cif以及消息本地事务模式提出的需求，即最后的sql是在哪个库执行的，以及该库的标识id
     */
    public static final String GET_ID_AND_DATABASE = "GET_ID_AND_DATABASE";
    /**
     * added by fanzeng, 支持 trade系统在按数据源key分组随机选择db时，根据autocommit属性来决定，以保证事务；
     */
    public static final String GET_DB_ORDER_IN_GROUP = "GET_DB_ORDER_IN_GROUP";
    public static final String GET_AUTOCOMMIT_PROPERTY = "GET_AUTOCOMMIT_PROPERTY";

    /**
     * added by fanzeng, 设置后，一组sql的执行db将会是同一个，如果发生了故障，就去找一个可用的。
     */
    public static final String SET_GROUP_SQL_DATABASE = "SET_GROUP_SQL_DATABASE";
    public static final String DB_NAME_USED_BY_GROUP_SQL = "DB_NAME_USED_BY_GROUP_SQL";
    /**
     * Once using elastic rule, this variable indicate elastic group id defined within group rule map
     */
    public static final String ELASTIC_GROUP_ID = "ELASTIC_GROUP_ID";
    /**
     * Once using elastic rule, this variable indicate a data source index defined within an elastic group
     */
    public static final String ELASTIC_DATASOURCE_INDEX = "ELASTIC_DATASOURCE_INDEX";

    public static final String UNIQUE_DATASOURCE_LINK = ".";

    public static final String TARGET_TABLE = "TARGET_TABLE";

    public static final String TARGET_DATASOURCE = "TARGET_DATASOURCE";

    public static final String ZDAL_UNIQUE_DB_ID = ".";

    public static final String PAGINATION_CONDITION = "ZDAL_PAGINATION_CONDITION";

    /**
     *
     */
    public static final String APP_DS_CONTEXT_SUFFIX = "_ZDAL_DS_CONTEXT";

    public static final String APP_DS_NAME = "APP_DS_NAME";

    /**
     * 实际运行sql 语句
     */
    public static final String ACTUAL_EXECUTE_SQL = "_ACT_EXE_SQL";
}
