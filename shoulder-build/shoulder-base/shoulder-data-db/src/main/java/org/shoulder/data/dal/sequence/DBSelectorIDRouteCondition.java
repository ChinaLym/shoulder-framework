package org.shoulder.data.dal.sequence;

import java.util.ArrayList;
import java.util.List;

public class DBSelectorIDRouteCondition {

    final String       dbSelectorID;
    final String       logicTableName;
    final List<String> tableList = new ArrayList<>();

    /**
     * 逻辑表名和目标表名完全一致的简化方法
     */
    public DBSelectorIDRouteCondition(String logicTableName, String dbSelectorID) {
        this(logicTableName, dbSelectorID, logicTableName);
    }

    /**
     * 建立一个直接通过逻辑表名，数据库执行id和实际表名，执行SQL的RouteCondition
     */
    public DBSelectorIDRouteCondition(String logicTableName, String logicalDataSourceID,
                                      String... tables) {
        this.dbSelectorID = logicalDataSourceID;
        this.logicTableName = logicTableName;
        List<String> list = List.of(tables);
        tableList.addAll(list);
    }

    /**
     * Return selected database id
     */
    public String getDBSelectorID() {
        return dbSelectorID;
    }


    public String getVirtualTableName() {
        return logicTableName;
    }

}
