package org.shoulder.web.template.crud;


import org.shoulder.core.context.AppContext;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.service.BaseService;
import org.shoulder.log.operation.support.OperableObjectTypeRepository;
import org.shoulder.web.template.dictionary.base.ShoulderConversionService;

import java.io.Serializable;

/**
 * 基础，几个基本方法，不提供任何接口
 *
 * @author lym
 */
public interface BaseController<ENTITY extends BaseEntity<? extends Serializable>> {

    /**
     * 获取实体类型
     *
     * @return 实体的类型
     */
    Class<ENTITY> getEntityClass();

    /**
     * 获取 Service
     *
     * @return Service
     */
    BaseService<ENTITY> getService();

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

    /**
     * 获取对象类型
     * 用于记录操作日志
     *
     * @return 对象类型
     */
    default String getEntityObjectType() {
        return ContextUtils.getBean(OperableObjectTypeRepository.class).getObjectType(getEntityClass());
    }

    ShoulderConversionService getConversionService();
}
