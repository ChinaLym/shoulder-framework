package org.shoulder.web.template.dictionary.base;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.spi.String2ConfigAbleDictionaryItemConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 【放shoulder 推荐，也可使用者自己】
 *
 * @param <S>
 * @param <T>
 */
public abstract class BaseDataConverter<S, T> implements Converter<S, T> {

    /**
     *
     * targetModel\.set(.*?)\(\)\;
     * targetModel.set$1(sourceModel.get$1());
     *
     * 去掉无意义 String 判空
     * if \(StringUtils\.isNotBlank\(sourceModel\.get(.*?)\(\)\)\) \{\n            targetModel\.set(.*?)\(sourceModel\.get(.*?)\(\)\)\;\n
     * \}
     * targetModel.set$1(sourceModel.get$1());
     *
     * 去掉 无意义 判 null
     * if \(sourceModel\.get(.*?)\(\) != null\) \{\n            targetModel\.set(.*?)\(sourceModel\.get(\w+?)\(\)\)\;\n        \}
     * targetModel.set$1(sourceModel.get$1());
     *
     * 去掉多余 getItemCode 前判空
     * if \(sourceModel\.get(.*?)\(\) != null\) \{\n            targetModel\.set(.*?)\(sourceModel\.get(.*?).getItemCode\(\)\)\;\n        \}
     * targetModel.set$1(conversionService.convert(sourceModel.get$1(), String.class));
     *
     *
     * \((.*?)\.getItemCode\(\)\)\;
     * (conversionService.convert($1, DictionaryItemVO.class));
     * 去掉多余 getItemCode 前判空
     */

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDataConverter.class);

    /**
     * 实体类型
     */
    protected final Class<S> sourceEntityClass;

    /**
     * 实体类型
     */
    protected final Class<T> targetEntityClass;

    protected boolean skipPreHandle;

    @Setter
    @Autowired
    protected ShoulderConversionService conversionService;

    @Autowired(required = false)
    protected String2ConfigAbleDictionaryItemConverter string2ConfigAbleDictionaryItemConverter;

    /**
     * 构造方法，约束泛型类型
     */
    public BaseDataConverter() {
        try {
            ParameterizedType parameterizedType = ((ParameterizedType) getClass()
                    .getGenericSuperclass());
            sourceEntityClass = (Class<S>) parameterizedType.getActualTypeArguments()[0];
            targetEntityClass = (Class<T>) parameterizedType.getActualTypeArguments()[1];
        } catch (Exception e) {
            throw new BaseRuntimeException("Undefined generics! Inherited from:" + BaseDataConverter.class.getCanonicalName()
                    + "must declare the generic type of the entity it operates on");
        }
    }

    /**
     * 源模型转换为目标模型
     *
     * @param sourceModel 源模型
     * @return 目标模
     */
    @Override
    public T convert(@Nullable S sourceModel) {
        if (sourceModel == null) {
            return null;
        }
        T targetModel;
        try {
            if (skipPreHandle()) {
                return doConvert(sourceModel);
            }
            targetModel = targetEntityClass.newInstance();
            // 先执行通用逻辑
            preHandle(sourceModel, targetModel);
            // 执行转换
            doConvert(sourceModel, targetModel);
        } catch (Exception e) {
            StringBuilder bf = new StringBuilder("param convert error, source type:");
            bf.append(sourceEntityClass.getSimpleName()).append(",target type:")
                    .append(targetEntityClass.getSimpleName());
            //LogUtil.error(LOGGER, e, bf.toString());
            // convertFail
            throw new BaseRuntimeException(CommonErrorCodeEnum.CODING, bf.toString(), e);
        }
        return targetModel;
    }

    /**
     * handle common fields
     */
    protected void preHandle(S sourceModel, T targetModel) {
        //if(BaseModel.class.isAssignableFrom(sourceEntityClass) && BaseDO.class.isAssignableFrom(targetEntityClass)){
        //    // domain -> do
        //    BaseModel domain = (BaseModel) sourceModel;
        //    BaseDO dataObj = (BaseDO) targetModel;
        //    dataObj.setId(domain.getId());
        //    // 设置逻辑删除字段
        //    dataObj.setDeleteFlag(domain.getDeleteFlag());
        //    if(domain.getGmtCreate() != null){
        //        dataObj.setGmtCreate(domain.getGmtCreate());
        //    }
        //    if(domain.getGmtModified() != null){
        //        dataObj.setGmtModified(domain.getGmtModified());
        //    }
        //
        //    if(domain.getCreator() != null && ReflectUtil.hasField(targetEntityClass, DataBaseConsts.FIELD_CREATOR)){
        //        ReflectUtil.setFieldValue(targetModel, DataBaseConsts.FIELD_CREATOR, domain.getCreator().getId());
        //        if(ReflectUtil.hasField(targetEntityClass, DataBaseConsts.FIELD_CREATOR_NAME)){
        //            ReflectUtil.setFieldValue(targetModel, DataBaseConsts.FIELD_CREATOR_NAME,domain.getCreator().getDisplayName());
        //        }
        //    }
        //    if(domain.getModifier() != null && ReflectUtil.hasField(targetEntityClass, DataBaseConsts.FIELD_MODIFIER)){
        //        ReflectUtil.setFieldValue(targetModel, DataBaseConsts.FIELD_MODIFIER, domain.getModifier().getId());
        //        if(ReflectUtil.hasField(targetEntityClass, DataBaseConsts.FIELD_MODIFIER_NAME)){
        //            ReflectUtil.setFieldValue(targetModel, DataBaseConsts.FIELD_MODIFIER_NAME, domain.getModifier().getDisplayName());
        //        }
        //    }
        //}
        //else if(BaseDO.class.isAssignableFrom(sourceEntityClass) && BaseModel.class.isAssignableFrom(targetEntityClass)){
        //    // do -> domain
        //    BaseDO dataObj = (BaseDO) sourceModel;
        //    BaseModel domain = (BaseModel) targetModel;
        //    domain.setId(dataObj.getId());
        //    domain.setVersion(0);
        //    domain.setGmtCreate(dataObj.getGmtCreate());
        //    domain.setGmtModified(dataObj.getGmtModified());
        //    // 设置逻辑删除字段
        //    domain.setDeleteFlag(dataObj.getDeleteFlag());
        //    if(ReflectUtil.hasField(sourceEntityClass, "version")){
        //        Integer version = (Integer) ReflectUtil.getFieldValue(sourceModel, "version");
        //        domain.setVersion(version);
        //    }
        //    //  convert 操作者
        //    Operator operator;
        //    if(ReflectUtil.hasField(sourceEntityClass, DataBaseConsts.FIELD_CREATOR)){
        //        String operatorId = (String) ReflectUtil.getFieldValue(sourceModel, DataBaseConsts.FIELD_CREATOR);
        //        if(operatorId != null) {
        //            operator = new Operator();
        //            operator.setId(operatorId);
        //            if(ReflectUtil.hasField(sourceEntityClass, DataBaseConsts.FIELD_CREATOR_NAME)){
        //                operator.setDisplayName((String) ReflectUtil.getFieldValue(sourceModel, DataBaseConsts.FIELD_CREATOR_NAME));
        //            }
        //            domain.setCreator(operator);
        //        }
        //    }
        //
        //    if(ReflectUtil.hasField(sourceEntityClass, DataBaseConsts.FIELD_MODIFIER)){
        //        String operatorId = (String) ReflectUtil.getFieldValue(sourceModel, DataBaseConsts.FIELD_MODIFIER);
        //        if(operatorId != null) {
        //            operator = new Operator();
        //            operator.setId(operatorId);
        //            if(ReflectUtil.hasField(sourceEntityClass, DataBaseConsts.FIELD_MODIFIER_NAME)){
        //                operator.setDisplayName((String) ReflectUtil.getFieldValue(sourceModel, DataBaseConsts.FIELD_MODIFIER_NAME));
        //            }
        //            domain.setModifier(operator);
        //        }
        //    }
        //}
    }

    /**
     * 源模型转换为目标模型(List)
     *
     * @param sourceModels 源模型列表
     * @return 目标模型列表
     */
    public List<T> convert(@Nullable Collection<? extends S> sourceModels) {
        if (sourceModels == null) {
            return null;
        }
        List<T> result = new ArrayList<>(sourceModels.size());
        for (S dtoData : sourceModels) {
            result.add(convert(dtoData));
        }
        return result;
    }

    /**
     * 执行具体的模型转换
     * 公共字段无特殊需求不需要转（已经自动转换）
     *
     * @param sourceModel 源模型
     * @param targetModel 目标模型
     */
    public abstract void doConvert(@NotNull S sourceModel, @NotNull T targetModel);

    /**
     * 是否跳过预处理
     *
     * @return 默认 false
     * @see #doConvert(Object) 跳过时必须实现
     */
    protected boolean skipPreHandle() {
        return skipPreHandle;
    }

    /**
     * 执行具体的模型转换
     *
     * @param sourceModel 源模型
     * @return targetModel 目标模型
     * @see #skipPreHandle true 时必须实现
     */
    @Nullable
    public T doConvert(@NotNull S sourceModel) {
        throw new IllegalStateException("please implements the method when skipPreHandle() return true!");
    }

    protected ConfigAbleDictionaryItem convertToConfigAbleDictionaryItem(String dictionaryCode, String code) {
        AssertUtils.notNull(string2ConfigAbleDictionaryItemConverter, CommonErrorCodeEnum.CODING,
                "No qualifying bean of type 'org.lym.pom.dto.moments.convert.base.String2ConfigAbleDictionaryItemConverter' available: expected at least 1 bean which qualifies as autowire candidate.");
        return string2ConfigAbleDictionaryItemConverter.convertToConfigAbleDictionaryItem(dictionaryCode, code);
    }

}
