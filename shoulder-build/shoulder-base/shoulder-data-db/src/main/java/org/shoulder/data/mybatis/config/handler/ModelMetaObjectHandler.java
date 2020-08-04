package org.shoulder.data.mybatis.config.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * 自动填充 创建时间、更新时间等 todo 添加创建者、更新者
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
        if (null == createTime) {
            this.setFieldValByName("createTime", now, metaObject);
        }
        /*this.setFieldValByName("updateTime", new Date(), metaObject);*/
    }

    /**
     * 更新时添加更新时间
     *
     * @param metaObject 页面传递过来的参数的包装对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }
}
