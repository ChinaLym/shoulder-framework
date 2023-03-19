package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.extension.injector.methods.AlwaysUpdateSomeColumnById;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;

import java.util.function.Predicate;

/**
 * 修改所有的字段
 * alwaysUpdateSomeColumnById 别名，原含义为更新时总是更新特定的字段，即使为 null
 * - 不设置值的，我都想更新为null，而不是忽略
 * - 更新时要始终排除某个字段，防止被误更新
 *
 * @author lym
 * @see BaseMapper#updateAllFieldsById
 */
@SuppressWarnings("serial")
public class UpdateAllFieldsById extends AlwaysUpdateSomeColumnById {

    public UpdateAllFieldsById(final Predicate<TableFieldInfo> predicate) {
        super(DataBaseConsts.METHOD_UPDATE_ALL_FIELDS_BY_ID, predicate);
    }

}
