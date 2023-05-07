package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.LogicDeleteEntity;

/**
 * 批量逻辑删除
 * where id in idList
 *
 * @author lym
 * @see BaseMapper
 * @see LogicDeleteEntity
 */
@SuppressWarnings("serial")
public class DeleteInLogicByIdList extends AbstractDeleteInLogicMethod {

    /**
     * @since 3.5.0
     */
    public DeleteInLogicByIdList() {
        super(DataBaseConsts.METHOD_DELETE_LOGIC_BY_ID_LIST);
    }

    @Override
    protected String genWhereSql(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        return "id in <foreach collection='idList' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
                " AND delete_version=0";
    }

}
