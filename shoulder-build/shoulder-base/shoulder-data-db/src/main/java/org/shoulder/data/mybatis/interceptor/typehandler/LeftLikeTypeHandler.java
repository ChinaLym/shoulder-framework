package org.shoulder.data.mybatis.interceptor.typehandler;


import com.baomidou.mybatisplus.core.enums.SqlLike;
import org.apache.ibatis.type.Alias;

/**
 * 前模糊
 *
 * @author lym
 */
@Alias("leftLike")
public class LeftLikeTypeHandler extends BaseLikeTypeHandler {

    public LeftLikeTypeHandler() {
        super(SqlLike.LEFT);
    }

}
