package org.shoulder.web.template.crud;

import org.shoulder.core.util.ConvertUtil;
import org.shoulder.data.mybatis.template.service.BaseService;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.PostConstruct;
import java.io.Serializable;

/**
 * 提供基本的 CRUD API 能力
 *
 * @param <S>         Service
 * @param <Id>        主键
 * @param <ENTITY>    实体
 * @param <PageQuery> 分页参数
 * @param <SaveDTO>   保存参数
 * @param <UpdateDTO> 修改参数
 * @author lym
 */
public abstract class CrudController<S extends BaseService<ENTITY>, ENTITY, Id extends Serializable, PageQuery, SaveDTO, UpdateDTO>
        extends BaseControllerImpl<S, ENTITY>
        implements
        SaveController<ENTITY, SaveDTO>,
        UpdateController<ENTITY, UpdateDTO>,
        DeleteController<ENTITY, Id>,
        QueryController<ENTITY, Id, PageQuery> {

    @PostConstruct
    public void registerConvertor() {
        ConvertUtil.addConverter((Converter<PageQuery, ENTITY>) this::convertToEntity);
    }

}
