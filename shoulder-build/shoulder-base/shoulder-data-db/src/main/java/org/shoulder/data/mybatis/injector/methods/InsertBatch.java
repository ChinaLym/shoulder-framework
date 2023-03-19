package org.shoulder.data.mybatis.injector.methods;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.dao.BaseMapper;

import java.util.function.Predicate;

/**
 * 批量插入
 * 使用效率更高的语法 insert into table values (x1, y1, z1), (x2, y2, z2), (x…, y…, z…);
 *
 * @author lym
 * @see BaseMapper#insertBatch
 */
@SuppressWarnings("serial")
public class InsertBatch extends InsertBatchSomeColumn {


    public InsertBatch(final Predicate<TableFieldInfo> predicate) {
        super(DataBaseConsts.METHOD_INSERT_BATCH, predicate);
    }

}
