package org.shoulder.data.mybatis.interceptor.typehandler;

import com.baomidou.mybatisplus.core.enums.SqlLike;
import org.apache.ibatis.type.Alias;

/**
 * 左右模糊 查询
 *
 * @author lym
 */
@Alias("fullLike")
public class FullLikeTypeHandler extends BaseLikeTypeHandler {

    public FullLikeTypeHandler() {
        super(SqlLike.DEFAULT);
    }

}
