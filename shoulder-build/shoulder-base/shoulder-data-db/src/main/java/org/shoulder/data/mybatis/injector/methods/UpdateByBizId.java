package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.shoulder.data.constant.DataBaseConsts;

/**
 * 根据 bizId + version 更新有值字段
 *
 * @author lym
 * @see com.baomidou.mybatisplus.core.injector.methods.UpdateById
 */
@SuppressWarnings("serial")
public class UpdateByBizId extends AbstractMethod {

    public UpdateByBizId() {
        super(DataBaseConsts.METHOD_UPDATE_BY_BIZ_ID);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        SqlMethod sqlMethod = SqlMethod.UPDATE_BY_ID;
        final String additional = optlockVersion(tableInfo) + tableInfo.getLogicDeleteSql(true, true);
        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(),
                sqlSet(tableInfo.isWithLogicDelete(), false, tableInfo, false, ENTITY, ENTITY_DOT),
                // change here
                "biz_id", ENTITY_DOT + DataBaseConsts.FIELD_BIZ_ID, additional);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);
    }


}
