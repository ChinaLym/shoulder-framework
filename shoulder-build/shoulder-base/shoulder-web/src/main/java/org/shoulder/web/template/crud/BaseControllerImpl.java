package org.shoulder.web.template.crud;

import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.log.AppLoggers;
import org.shoulder.core.log.Logger;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.service.BaseService;

import java.io.Serializable;

/**
 * BaseController
 *
 * @param <ENTITY> 实体
 * @author lym
 */
public abstract class BaseControllerImpl<SERVICE extends BaseService<ENTITY>, ENTITY extends BaseEntity<? extends Serializable>> implements BaseController<ENTITY> {

    protected final Logger log = AppLoggers.APP_SERVICE;

    protected final SERVICE service;

    protected final ShoulderConversionService conversionService;

    protected BaseControllerImpl(SERVICE service, ShoulderConversionService conversionService) {
        this.service = service;
        this.conversionService = conversionService;
    }

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
