package org.shoulder.web.template.crud;

import org.shoulder.core.util.ConvertUtil;
import org.shoulder.data.mybatis.template.service.BaseService;

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
@SuppressWarnings("rawtypes")
public abstract class CrudController<S extends BaseService<ENTITY>, ENTITY, Id extends Serializable, PageQuery, SaveDTO, UpdateDTO>
        extends BaseControllerImpl<S, ENTITY>
        implements
        SaveController<ENTITY, SaveDTO>,
        UpdateController<ENTITY, UpdateDTO>,
        DeleteController<ENTITY, Id>,
        QueryController<ENTITY, Id, PageQuery> {

    protected Class[] genericClasses;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void registerConvertor() throws ClassNotFoundException {
        Class[] genericClasses = getGenericInfo();
        Class<ENTITY> entityClass = (Class<ENTITY>) genericClasses[1];
        Class<PageQuery> pageQueryClass = (Class<PageQuery>) genericClasses[3];
        ConvertUtil.addConverter(pageQueryClass, entityClass, this::convertToEntity);
    }

    protected Class[] getGenericInfo() throws ClassNotFoundException {
        if (genericClasses != null) {
            return genericClasses;
        }
        String genericInfo = getClass().getGenericSuperclass().toString();
        final String packagePrefix = "org.shoulder.web.template.crud.Crud";
        if (genericInfo.startsWith(packagePrefix)) {
            String[] genericClassesName = genericInfo.split("<", 2)[1].split(",");
            assert genericClassesName.length == 6;
            genericClassesName[1] = genericClassesName[1].substring(1);
            genericClassesName[2] = genericClassesName[2].substring(1);
            genericClassesName[3] = genericClassesName[3].substring(1);
            genericClassesName[4] = genericClassesName[4].substring(1);
            genericClassesName[5] = genericClassesName[5].substring(1, genericClassesName[5].length() - 1);
            Class[] classes = new Class[6];
            for (int i = 0; i < genericClassesName.length; i++) {
                classes[i] = Class.forName(genericClassesName[i]);
            }
            this.genericClasses = classes;
            return classes;
        } else {
            throw new ClassNotFoundException("getGenericInfo FAIL! Can't find genericInfo! superClass=" + genericInfo);
        }
    }

}
