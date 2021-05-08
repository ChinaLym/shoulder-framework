package org.shoulder.web.template;


import org.shoulder.core.context.AppContext;
import org.shoulder.data.mybatis.template.service.BaseService;

/**
 * 基础，几个基本方法，不提供任何接口
 *
 * @author lym
 */
public interface BaseController<Entity> {

    /**
     * 获取实体类型
     *
     * @return 实体的类型
     */
    Class<Entity> getEntityClass();

    /**
     * 获取 Service
     *
     * @return Service
     */
    BaseService<Entity> getService();

    // ============================ 常用的方法从框架工具类中抽离出来 ===========================

    /**
     * 获取当前id
     *
     * @return userId
     */
    default String getUserId() {
        return AppContext.getUserId();
    }

    /**
     * 当前请求租户
     *
     * @return 租户编码
     */
    default String getTenant() {
        return AppContext.getTenantCode();
    }

    /**
     * 登录人账号
     *
     * @return 账号
     */
    default String getUserName() {
        return AppContext.getUserName();
    }

}
