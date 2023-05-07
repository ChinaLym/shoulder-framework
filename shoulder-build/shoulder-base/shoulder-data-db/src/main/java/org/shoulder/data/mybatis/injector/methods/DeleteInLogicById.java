package org.shoulder.data.mybatis.injector.methods;

import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.LogicDeleteEntity;

/**
 * 逻辑删除
 * DELETE 方法改为 update xxx，只对 {@link LogicDeleteEntity} 生效
 *
 * @author lym
 * @see BaseMapper
 * @see LogicDeleteEntity
 */
@SuppressWarnings("serial")
public class DeleteInLogicById extends AbstractDeleteInLogicMethod {

    /**
     * @since 3.5.0
     */
    public DeleteInLogicById() {
        super(DataBaseConsts.METHOD_DELETE_LOGIC_BY_ID);
    }

}
