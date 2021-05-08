package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.extension.injector.methods.LogicDeleteByIdWithFill;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;

/**
 * 逻辑删除
 * DELETE 方法改为 update xxx
 *
 * @author lym
 * @see BaseMapper
 */
@SuppressWarnings("serial")
public class DeleteInLogic extends LogicDeleteByIdWithFill {

    public DeleteInLogic() {
        super();
    }

    @Override
    public String getMethod(SqlMethod sqlMethod) {
        // 对应 mapper 里的方法名
        return DataBaseConsts.METHOD_DELETE_LOGIC_BY_ID;
    }
}
