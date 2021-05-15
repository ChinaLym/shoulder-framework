package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.BizEntity;

import javax.annotation.Nullable;


/**
 * 逻辑删除，只对 {@link BizEntity} 生效
 *
 * @author lym
 * @see BaseMapper
 * @see BizEntity
 */
@SuppressWarnings("serial")
public class DeleteInLogicByBizIdList extends AbstractDeleteInLogicMethod {

    /**
     * 是否支持逻辑删除
     */
    @Override
    protected boolean support(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        return BizEntity.class.isAssignableFrom(modelClass);
    }

    /**
     * 这里未加 and version=version
     */
    @Override
    protected String genWhereSql(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        return "where biz_id in <foreach collection='entityList' item='entity' open='('separator=',' close=')'>#{entity.bizId}</foreach>" +
                " AND delete_version=0";
    }

    @Override
    public String getMethod(@Nullable SqlMethod sqlMethod) {
        // 对应 mapper 里的方法名
        return DataBaseConsts.METHOD_DELETE_LOGIC_BY_BIZ_ID_LIST;
    }

}
