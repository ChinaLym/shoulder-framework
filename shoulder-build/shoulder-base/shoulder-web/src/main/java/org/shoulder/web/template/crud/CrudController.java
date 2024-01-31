package org.shoulder.web.template.crud;

import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.entity.BizEntity;
import org.shoulder.data.mybatis.template.service.BaseService;
import org.shoulder.data.uid.BizIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

/**
 * 提供基本的 CRUD API 能力
 *
 * @param <SERVICE>   Service
 * @param <ID>        主键
 * @param <ENTITY>    实体
 * @param <PageQuery> 分页参数
 * @param <SaveDTO>   保存参数
 * @param <UpdateDTO> 修改参数
 * @author lym
 */
@SuppressWarnings("rawtypes")
@RestController
public abstract class CrudController<
    SERVICE extends BaseService<ENTITY>,
    ENTITY extends BaseEntity<ID>,
    ID extends Serializable,
    QueryResultDTO extends Serializable,
    PageQuery extends Serializable,
    SaveDTO extends Serializable,
    UpdateDTO extends Serializable>
        extends BaseControllerImpl<SERVICE, ENTITY>
        implements
        SaveController<ENTITY, SaveDTO, QueryResultDTO>,
        UpdateController<ENTITY, UpdateDTO, QueryResultDTO>,
        DeleteController<ENTITY, ID, QueryResultDTO>,
        QueryController<ENTITY, ID, PageQuery, QueryResultDTO> {

    protected Class[] genericClasses = GenericTypeResolver.resolveTypeArguments(getClass(), CrudController.class);

    @Autowired(required = false)
    protected BizIdGenerator bizIdGenerator;

    //@PostConstruct
    //@SuppressWarnings("unchecked")
    //public void registerConvertor() throws ClassNotFoundException {
    //    Class[] genericClasses = getGenericInfo();
    //    Class<ENTITY> entityClass = (Class<ENTITY>) genericClasses[1];
    //    Class<PageQuery> pageQueryClass = (Class<PageQuery>) genericClasses[4];
    //    ConvertUtil.addConverter(pageQueryClass, entityClass, this::convertToEntity);
    //}

    //protected Class[] getGenericInfo() throws ClassNotFoundException {
    //    if (genericClasses != null) {
    //        return genericClasses;
    //    }
    //    String genericInfo = getClass().getGenericSuperclass().toString();
    //    final String packagePrefix = "org.shoulder.web.template.crud.Crud";
    //    if (genericInfo.startsWith(packagePrefix)) {
    //        String[] genericClassesName = genericInfo.split("<", 2)[1].split(",");
    //        assert genericClassesName.length == 6;
    //        genericClassesName[1] = genericClassesName[1].substring(1);
    //        genericClassesName[2] = genericClassesName[2].substring(1);
    //        genericClassesName[3] = genericClassesName[3].substring(1);
    //        genericClassesName[4] = genericClassesName[4].substring(1);
    //        genericClassesName[5] = genericClassesName[5].substring(1, genericClassesName[5].length() - 1);
    //        Class[] classes = new Class[6];
    //        for (int i = 0; i < genericClassesName.length; i++) {
    //            classes[i] = Class.forName(genericClassesName[i]);
    //        }
    //        this.genericClasses = classes;
    //        return classes;
    //    } else {
    //        throw new ClassNotFoundException("getGenericInfo FAIL! Can't find genericInfo! superClass=" + genericInfo);
    //    }
    //}

    @Override
    @SuppressWarnings("unchecked")
    public String generateBizId(ENTITY entity) {
        getService().checkEntityAs(BizEntity.class);
        return bizIdGenerator.generateBizId((BizEntity) entity, (Class<? extends BizEntity>) entity.getClass());
    }

    @Override public Class<QueryResultDTO> getQueryDtoClass() {
        return genericClasses[3];
    }
}
