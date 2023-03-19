package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.shoulder.data.constant.DataBaseConsts;

/**
 * 根据 id 锁定行，利用悲观锁 for update
 * 当且仅当事务内锁定：使用时务必主动管理事务，否则执行完自动提交将自动释放锁定
 *
 * @author lym
 */
@SuppressWarnings("serial")
public class SelectBatchForUpdateByBizIds extends AbstractMethod {

    public SelectBatchForUpdateByBizIds() {
        super(DataBaseConsts.METHOD_SELECT_BATCH_FOR_UPDATE_BY_BIZ_IDS);
    }

    private static final String SQL_SELECT_FOR_UPDATE = "<script>SELECT %s FROM %s WHERE biz_id IN (%s) %s FOR UPDATE</script>";

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.SELECT_BATCH_BY_IDS;
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, String.format(sqlMethod.getSql(),
                sqlSelectColumns(tableInfo, false), tableInfo.getTableName(),
                // change here
                "biz_id",
                SqlScriptUtils.convertForeach("#{item}", COLL, null, "item", COMMA),
                tableInfo.getLogicDeleteSql(true, true)), Object.class);
        return addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
    }
}