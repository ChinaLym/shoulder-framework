package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.shoulder.data.constant.DataBaseConsts;

/**
 * 根据ID 查询一条数据
 *
 * @author lym
 * @see com.baomidou.mybatisplus.core.injector.methods.SelectById
 */
@SuppressWarnings("serial")
public class SelectByBizId extends AbstractMethod {

    /**
     * @since 3.5.0
     */
    public SelectByBizId() {
        super(DataBaseConsts.METHOD_SELECT_BY_BIZ_ID);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.SELECT_BY_ID;
        SqlSource sqlSource = new RawSqlSource(configuration, String.format(sqlMethod.getSql(),
                sqlSelectColumns(tableInfo, false),
                tableInfo.getTableName(),
                // change here
                "biz_id", "bizId",
                tableInfo.getLogicDeleteSql(true, true)), Object.class);
        return this.addSelectMappedStatementForTable(mapperClass, methodName, sqlSource, tableInfo);
    }

}
