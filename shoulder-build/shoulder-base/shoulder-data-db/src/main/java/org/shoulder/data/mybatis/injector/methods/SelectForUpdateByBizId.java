package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.shoulder.data.constant.DataBaseConsts;

/**
 * 根据 id 锁定行，利用悲观锁 for update
 * 当且仅当事务内锁定：使用时务必主动管理事务，否则执行完自动提交将自动释放锁定
 *
 * @author lym
 * @see SelectForUpdateById
 */
@SuppressWarnings("serial")
public class SelectForUpdateByBizId extends AbstractMethod {

    public SelectForUpdateByBizId() {
        super(DataBaseConsts.METHOD_SELECT_FOR_UPDATE_BY_BIZ_ID);
    }

    private static final String SQL_SELECT_FOR_UPDATE = SqlMethod.SELECT_BY_ID.getSql() + " FOR UPDATE";

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        TableFieldInfo bizIdFieldInfo = tableInfo.getFieldList().stream()
                .filter(f -> "bizId".equals(f.getProperty()))
                .findFirst()
                .orElse(null);
        if (bizIdFieldInfo == null) {
            logger.warn("not support such entity(" + modelClass.getName() +
                    ") for can't find property named bizId!");
        }
        SqlSource sqlSource = new RawSqlSource(
                this.configuration,
                String.format(SQL_SELECT_FOR_UPDATE,
                        this.sqlSelectColumns(tableInfo, false),
                        tableInfo.getTableName(),
                        // change here
                        "biz_id", "bizId",
                        // 只取未删除的
                        tableInfo.getLogicDeleteSql(true, true)
                ),
                Object.class
        );

        return this.addSelectMappedStatementForTable(mapperClass, DataBaseConsts.METHOD_SELECT_FOR_UPDATE_BY_BIZ_ID, sqlSource, tableInfo);
    }

}