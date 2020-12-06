package org.shoulder.data.mybatis.config.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.StringUtils;

import java.util.Date;

/**
 * 自动填充 创建时间、更新时间等
 *
 * @author lym
 */
public class ModelMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时添加创建时间
     *
     * @param metaObject 页面传递过来的参数的包装对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        Object createTime = this.getFieldValByName("createTime", metaObject);
        if (createTime == null) {
            this.setFieldValByName("createTime", now, metaObject);
        }
        String currentUserIdStr = AppContext.getUserIdAsString();
        if (StringUtils.isNotEmpty(currentUserIdStr)) {
            Long currentUserId = Long.valueOf(currentUserIdStr);
            Object creator = this.getFieldValByName("creator", metaObject);
            if (creator == null) {
                this.setFieldValByName("creator", currentUserId, metaObject);
            }
        }
    }

    /**
     * 更新时添加更新时间，修改人
     *
     * @param metaObject 页面传递过来的参数的包装对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Date now = new Date();
        Object updateTime = this.getFieldValByName("updateTime", metaObject);
        if (updateTime == null) {
            this.setFieldValByName("updateTime", now, metaObject);
        }
        String currentUserIdStr = AppContext.getUserIdAsString();
        if (StringUtils.isNotEmpty(currentUserIdStr)) {
            Long currentUserId = Long.valueOf(currentUserIdStr);
            Object modifer = this.getFieldValByName("modifer", metaObject);
            if (modifer == null) {
                this.setFieldValByName("modifer", currentUserId, metaObject);
            }
        }
    }
}
