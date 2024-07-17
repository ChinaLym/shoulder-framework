package org.shoulder.data.mybatis.injector;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.injector.methods.DeleteInLogicByBizId;
import org.shoulder.data.mybatis.injector.methods.DeleteInLogicByBizIdList;
import org.shoulder.data.mybatis.injector.methods.DeleteInLogicById;
import org.shoulder.data.mybatis.injector.methods.DeleteInLogicByIdList;
import org.shoulder.data.mybatis.injector.methods.InsertBatch;
import org.shoulder.data.mybatis.injector.methods.SelectBatchByBizIds;
import org.shoulder.data.mybatis.injector.methods.SelectBatchForUpdateByBizIds;
import org.shoulder.data.mybatis.injector.methods.SelectByBizId;
import org.shoulder.data.mybatis.injector.methods.SelectForUpdateByBizId;
import org.shoulder.data.mybatis.injector.methods.SelectForUpdateById;
import org.shoulder.data.mybatis.injector.methods.UpdateAllFieldsById;
import org.shoulder.data.mybatis.injector.methods.UpdateByBizId;

import java.util.List;

/**
 * 让 mybatis-plus 为我们生成一些额外方法
 *
 * @author lym
 */
public class ShoulderSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);

        // 批量插入
        methodList.add(new InsertBatch(i -> i.getFieldFill() != FieldFill.UPDATE));
        // 根据 id 锁定
        methodList.add(new SelectForUpdateById());
        // 根据 id 更新所有字段
        methodList.add(new UpdateAllFieldsById(field -> !ArrayUtil.containsAny(new String[]{
                DataBaseConsts.COLUMN_CREATE_TIME,
                DataBaseConsts.COLUMN_CREATOR
        }, field.getColumn())));

        // 逻辑删除
        methodList.add(new DeleteInLogicById());
        methodList.add(new DeleteInLogicByIdList());

        // bizId 扩展
        methodList.add(new SelectByBizId());
        methodList.add(new SelectForUpdateByBizId());
        methodList.add(new SelectBatchByBizIds());
        methodList.add(new SelectBatchForUpdateByBizIds());

        methodList.add(new UpdateByBizId());

        methodList.add(new DeleteInLogicByBizId());
        methodList.add(new DeleteInLogicByBizIdList());

        return methodList;
    }

}
