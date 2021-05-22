package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.LogicDeleteByIdWithFill;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.LogicDeleteEntity;

/**
 * 逻辑删除
 * DELETE 方法改为 update xxx，只对 {@link LogicDeleteEntity} 生效
 *
 * @author lym
 * @see BaseMapper
 * @see LogicDeleteByIdWithFill
 * @see SqlMethod#LOGIC_DELETE_BY_ID
 * @see LogicDeleteEntity
 */
@SuppressWarnings("serial")
public abstract class AbstractDeleteInLogicMethod extends AbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        if (!support(mapperClass, modelClass, tableInfo)) {
            // 非 shoulder 定义的实体类
            // <script>\nUPDATE %s %s WHERE %s=#{%s} %s\n</script>
            //return super.injectMappedStatement(mapperClass, modelClass, tableInfo);
            logger.warn("not support such entity(" + modelClass.getName() +
                    ") for not extends " + LogicDeleteEntity.class.getName());
        }

        String sql = String.format(baseSql(), tableInfo.getTableName(),
                // set todo 需要 version 增加
                genSetSql(mapperClass, modelClass, tableInfo),
                // where 不需要判断 version
                genWhereSql(mapperClass, modelClass, tableInfo)
        );
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return addUpdateMappedStatement(mapperClass, modelClass, getMethod(null), sqlSource);
    }

    /**
     * 是否支持逻辑删除
     */
    protected boolean support(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        // && tableInfo.isWithLogicDelete();
        return LogicDeleteEntity.class.isAssignableFrom(modelClass);
    }

    /**
     * 基础 sql
     */
    protected String baseSql() {
        return "<script>\nUPDATE %s %s WHERE %s\n</script>";
    }

    /**
     * set sql
     * SET delete_version=NOW()
     */
    protected String genSetSql(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        //return "SET " + tableInfo.getLogicDeleteFieldInfo().getColumn() + "=NOW()";
        return "SET delete_version=NOW()";
    }

    /**
     * where sql 条件
     * id = #{id} and delete_version=0
     */
    protected String genWhereSql(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        return "id=#{id} AND delete_version=0";
        /*TableFieldInfo logicDeleteField = tableInfo.getLogicDeleteFieldInfo();
        return tableInfo.getKeyColumn() + "=#{" + tableInfo.getKeyProperty() + "} AND " +
                logicDeleteField.getColumn() + "=" + logicDeleteField.getLogicNotDeleteValue();*/
    }

}
