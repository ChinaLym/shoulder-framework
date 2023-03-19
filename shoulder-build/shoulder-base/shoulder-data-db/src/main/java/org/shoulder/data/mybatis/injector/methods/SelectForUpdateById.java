package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
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
 */
@SuppressWarnings("serial")
public class SelectForUpdateById extends AbstractMethod {

    public SelectForUpdateById() {
        super(DataBaseConsts.METHOD_SELECT_FOR_UPDATE_BY_ID);
    }

    private static final String SQL_SELECT_FOR_UPDATE = SqlMethod.SELECT_BY_ID.getSql() + " FOR UPDATE";

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlSource sqlSource = new RawSqlSource(
                this.configuration,
                String.format(SQL_SELECT_FOR_UPDATE,
                        this.sqlSelectColumns(tableInfo, false),
                        tableInfo.getTableName(),
                        tableInfo.getKeyColumn(),
                        tableInfo.getKeyProperty(),
                        // 只取未删除的
                        tableInfo.getLogicDeleteSql(true, true)
                ),
                Object.class
        );

        return this.addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
    }

}