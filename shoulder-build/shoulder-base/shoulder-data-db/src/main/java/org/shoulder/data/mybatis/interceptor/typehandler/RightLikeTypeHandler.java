package org.shoulder.data.mybatis.interceptor.typehandler;


import com.baomidou.mybatisplus.core.enums.SqlLike;
import org.apache.ibatis.type.Alias;

/**
 * 后模糊
 *
 * @author lym
 */
@Alias("rightLike")
public class RightLikeTypeHandler extends BaseLikeTypeHandler {

    public RightLikeTypeHandler() {
        super(SqlLike.RIGHT);
    }

}

