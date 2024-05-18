package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.baomidou.mybatisplus.extension.injector.methods.LogicDeleteByIdWithFill;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.LogicDeleteEntity;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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

    /**
     * @param methodName 方法名
     * @since 3.5.0
     */
    protected AbstractDeleteInLogicMethod(String methodName) {
        super(methodName);
    }

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        if (!support(mapperClass, modelClass, tableInfo)) {
            // 非 shoulder 定义的实体类
            // <script>\nUPDATE %s %s WHERE %s=#{%s} %s\n</script>
            //return super.injectMappedStatement(mapperClass, modelClass, tableInfo);
            logger.warn("not support such entity(" + modelClass.getName() +
                    ") for not extends " + LogicDeleteEntity.class.getName());
        }

        // 更新时间、更信人等字段需要update
        List<TableFieldInfo> autoFillOnUpdateFieldsExceptDeleteFlag = tableInfo.getFieldList().stream()
                .filter(TableFieldInfo::isWithUpdateFill)
                .filter(f -> !f.isLogicDelete())
                .collect(toList());
        // 删除操作往往不 care 数据可修改的内容，故删除操作不care版本号，而更新又会判断是否删除，故删除不需要关住 version
        String sqlSet = CollectionUtils.isEmpty(autoFillOnUpdateFieldsExceptDeleteFlag) ?
                sqlLogicSet(tableInfo) :
                "SET " + SqlScriptUtils.convertIf(autoFillOnUpdateFieldsExceptDeleteFlag.stream()
                        .map(i -> i.getSqlSet(EMPTY)).collect(joining(EMPTY)), "!@org.apache.ibatis.type.SimpleTypeRegistry@isSimpleType(_parameter.getClass())", true)
                        // deleteVersion=id
                        + tableInfo.getLogicDeleteSql(false, false);


        String sql = String.format(baseSql(), tableInfo.getTableName(), sqlSet,
                genWhereSql(mapperClass, modelClass, tableInfo) + tableInfo.getLogicDeleteSql(true, true));

        SqlSource sqlSource = super.createSqlSource(configuration, sql, Object.class);
        return addUpdateMappedStatement(mapperClass, modelClass, methodName, sqlSource);
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
    // set todo P0 需要 version 增加
    protected String genSetSql(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        // SET delete_version = id
        return "SET " + tableInfo.getLogicDeleteFieldInfo().getColumn() + "=" + tableInfo.getKeyColumn();
    }

    /**
     * where sql 条件
     * id = #{id} and delete_version=0
     */
    protected String genWhereSql(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        return "id=#{id} ";
    }

}
