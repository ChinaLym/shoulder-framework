package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.BizEntity;

/**
 * 逻辑删除，只对 {@link BizEntity} 生效
 *
 * @author lym
 * @see BaseMapper
 * @see BizEntity
 */
@SuppressWarnings("serial")
public class DeleteInLogicByBizId extends AbstractDeleteInLogicMethod {

    /**
     * @since 3.5.0
     */
    public DeleteInLogicByBizId() {
        super(DataBaseConsts.METHOD_DELETE_LOGIC_BY_BIZ_ID);
    }

    /**
     * 是否支持逻辑删除
     */
    @Override
    protected boolean support(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        return BizEntity.class.isAssignableFrom(modelClass);
    }

    @Override
    protected String genWhereSql(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        return "biz_id=#{bizId} AND delete_version=0";
    }


}
