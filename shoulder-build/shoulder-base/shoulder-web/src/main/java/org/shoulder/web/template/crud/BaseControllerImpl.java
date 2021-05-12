package org.shoulder.web.template.crud;

import org.shoulder.data.mybatis.template.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * BaseController
 *
 * @param <ENTITY> 实体
 * @author lym
 */
public abstract class BaseControllerImpl<S extends BaseService<ENTITY>, ENTITY> implements BaseController<ENTITY> {

    @Autowired
    protected S service;

    @Override
    public Class<ENTITY> getEntityClass() {
        return getService().getEntityClass();
    }

    @Override
    public BaseService<ENTITY> getService() {
        return service;
    }

}
