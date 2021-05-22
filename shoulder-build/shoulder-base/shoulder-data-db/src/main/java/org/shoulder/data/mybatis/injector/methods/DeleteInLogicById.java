package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.LogicDeleteEntity;

import javax.annotation.Nullable;

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

    @Override
    public String getMethod(@Nullable SqlMethod sqlMethod) {
        // 对应 mapper 里的方法名
        return DataBaseConsts.METHOD_DELETE_LOGIC_BY_ID;
    }

}
