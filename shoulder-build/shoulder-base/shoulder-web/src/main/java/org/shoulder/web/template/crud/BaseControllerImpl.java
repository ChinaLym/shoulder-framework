package org.shoulder.web.template.crud;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.service.BaseService;
import org.shoulder.web.template.dictionary.base.ShoulderConversionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

/**
 * BaseController
 *
 * @param <ENTITY> 实体
 * @author lym
 */
public abstract class BaseControllerImpl<SERVICE extends BaseService<ENTITY>, ENTITY extends BaseEntity<? extends Serializable>> implements BaseController<ENTITY> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected SERVICE service;

    @Autowired
    protected ShoulderConversionService conversionService;

    @Override
    public Class<ENTITY> getEntityClass() {
        return getService().getEntityClass();
    }

    @Override
    public BaseService<ENTITY> getService() {
        return service;
    }

    @Override
    public ShoulderConversionService getConversionService() {
        return conversionService;
    }
}
