package org.shoulder.data.dal.sequence.model;

import lombok.Data;
import org.shoulder.core.context.AppContext;
import org.shoulder.data.dal.sequence.DBSelectorIDRouteCondition;

import java.io.Serializable;
import java.util.Map;

/**
 * Sequence 路由信息，确认到哪个库表
 *
 * @author lym
 */
@Deprecated(since = "暂不支持分库分表")
@Data
public class SequenceRouteInfo implements Serializable {

    String databaseIndex;

    String databaseShardValue;

    String logicalTableName;

    String tableShardValue;

    String partitionId;

    String targetTableName;

    String elasticDataSourceIndex;

    DBSelectorIDRouteCondition routeCondition;

    String logicDBIndex;

    Map<String, Serializable> allContext = AppContext.getAll();


    /**
     * 记录上下文信息，给 {@link org.shoulder.data.dal.sequence.monitor.SequenceRefreshRunnable} 使用
     */
    public void captureAppContext() {
        this.allContext = AppContext.getAll();
    }

    @Deprecated(since = "only for inner")
    public void setElasticDataSourceIndex(String elasticDataSourceIndex) {
        this.elasticDataSourceIndex = elasticDataSourceIndex;
    }

}
