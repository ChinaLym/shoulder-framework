package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;

import java.util.function.Predicate;

/**
 * 修改所有的字段
 * alwaysUpdateSomeColumnById 别名，原含义为更新时总是更新特定的字段，即使为 null
 *
 * @author lym
 * @see BaseMapper#updateAllById
 */
@SuppressWarnings("serial")
public class UpdateAllById extends AlwaysUpdateSomeColumnById {

    public UpdateAllById() {
        super();
    }

    public UpdateAllById(final Predicate<TableFieldInfo> predicate) {
        super(predicate);
    }

    @Override
    public String getMethod(SqlMethod sqlMethod) {
        // 对应 mapper 里的方法名
        return DataBaseConsts.METHOD_UPDATE_ALL_FIELDS_BY_ID;
    }
}
