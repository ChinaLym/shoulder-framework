package org.shoulder.data.mybatis.template.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.AppLoggers;
import org.shoulder.core.log.Logger;
import org.shoulder.core.util.ReflectionKit;
import org.shoulder.core.util.StringUtils;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.entity.BizEntity;
import org.shoulder.data.uid.EntityIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 通用业务实现类
 *
 * @author lym
 */
public abstract class BaseServiceImpl<MAPPER extends BaseMapper<ENTITY>,
        ENTITY extends BaseEntity<? extends Serializable>>
        extends ServiceImpl<MAPPER, ENTITY>
        implements BaseService<ENTITY> {

    protected Logger logger = AppLoggers.APP_BIZ;

    /**
     * 转换
     */
    @Autowired
    protected ShoulderConversionService conversionService;

    /**
     * idG
     */
    @Autowired
    protected EntityIdGenerator entityIdGenerator;

    protected Class<ENTITY> modelClass = resolveModelClass();

    protected static final String SQL_FOR_UPDATE = " FOR UPDATE";

    protected static final String SQL_LIMIT = " LIMIT ";

    // ==================== Type =====================

    @Override
    public ConversionService getConversionService() {
        return conversionService;
    }

    @Override
    public Class<ENTITY> getModelClass() {
        return modelClass;
    }

    @Override
    @SuppressWarnings("unchecked, rawtypes")
    protected Class currentMapperClass() {
        return ReflectionKit.getSuperClassGenericType(this.getClass(), BaseServiceImpl.class, 0);
    }

    @Override
    @SuppressWarnings("unchecked, rawtypes")
    protected Class currentModelClass() {
        return ReflectionKit.getSuperClassGenericType(this.getClass(), BaseServiceImpl.class, 1);
    }

    @SuppressWarnings("unchecked")
    protected Class<ENTITY> resolveModelClass() {
        return (Class<ENTITY>) currentModelClass();
    }

    @Override
    public EntityIdGenerator getIdGenerator() {
        return entityIdGenerator;
    }

    // ==================== Query =====================

    /*@Override
    public ENTITY queryByMultiCondition(ENTITY model, ExtendMap extend) {
        ENTITY dataObj = getConversionService().convert(model, getEntityClass());
        Wrapper<ENTITY> queryWrapper = generateMultiConditionQueryWarp(dataObj, extend);
        ENTITY dataObjInDb = getBaseMapper().selectOne(queryWrapper);
        return getConversionService().convert(dataObjInDb, getModelClass());
    }

    @Override
    public ENTITY lockByMultiCondition(ENTITY model, @NotNull ExtendMap extend) {
        ENTITY dataObj = getConversionService().convert(model, getEntityClass());
        extend = extend == null ? new ExtendMap() : extend;
        extend.forUpdate();
        Wrapper<ENTITY> queryWrapper = generateMultiConditionQueryWarp(dataObj, extend);
        ENTITY dataObjInDb = getBaseMapper().selectOne(queryWrapper);
        return getConversionService().convert(dataObjInDb, getModelClass());
    }

    @Override
    public List<ENTITY> lockListByMultiCondition(ENTITY example, @NotNull ExtendMap extend) {
        ENTITY dataObj = getConversionService().convert(example, getEntityClass());
        extend = extend == null ? new ExtendMap() : extend;
        extend.forUpdate();
        Wrapper<ENTITY> queryWrapper = generateMultiConditionQueryWarp(dataObj, extend);
        List<ENTITY> dataObjList = getBaseMapper().selectList(queryWrapper);
        return getConversionService().convert(dataObjList, getModelClass());
    }*/

    protected <T> Wrapper<T> withLast(Wrapper<T> queryWrapper, String last) {
        if (queryWrapper instanceof LambdaQueryWrapper) {
            ((LambdaQueryWrapper<T>) queryWrapper).last(last);
        } else if (queryWrapper instanceof QueryWrapper) {
            ((QueryWrapper<T>) queryWrapper).last(last);
        }
        return queryWrapper;
    }

    /*@Override
    public List<ENTITY> queryListByMultiCondition(ENTITY example, ExtendMap ext) {
        ENTITY dataObj = getConversionService().convert(example, getEntityClass());
        List<ENTITY> dataObjList = getBaseMapper()
                .selectList(generateMultiConditionQueryWarp(dataObj, ext));
        return getConversionService().convert(dataObjList, getModelClass());
    }

    @SuppressWarnings("unchecked, rawtypes")
    @Override
    public LambdaQueryWrapper<ENTITY> generateMultiConditionQueryWarp(ENTITY dataObj,
                                                                        @Nullable ExtendMap ext) {
        // 查询条件
        QueryWrapper<ENTITY> wrapper = new QueryWrapper<>(dataObj);

        List<SFunction<ENTITY, ?>> briefFields = null;

        // ext
        if (MapUtils.isNotEmpty(ext)) {
            for (Map.Entry<String, Object> extEntry : ext.entrySet()) {
                String key = extEntry.getKey();
                Object value = extEntry.getValue();
                if (ObjectUtil.isEmpty(value)) {
                    continue;
                }
                if (key.equals(ExtendMap.SELECT_PARTIAL) || key.equals(ExtendMap.SELECT_FIELDS)) {
                    // 只查询部分字段
                    if (value instanceof Collection) {
                        if (CollectionUtils.isNotEmpty((Collection<?>) value)) {
                            briefFields = (List<SFunction<ENTITY, ?>>) value;
                        }
                    } else {
                        briefFields = getBriefSelectFields(value);
                    }
                }
                // 不等于
                if (key.endsWith(ExtendMap.MARK_NOT_EQUALS)) {
                    // 不等于
                    AssertUtils.isTrue(ObjectUtil.isNotEmpty(value), CommonErrorCodeEnum.UNKNOWN);
                    Field field = getFieldWithSuffix(key, ExtendMap.MARK_NOT_EQUALS);
                    checkUse(dataObj, field.getName());
                    wrapper.ne(calculateDbColumnName(field.getName()),
                            getConversionService().convert(value, field.getType()));
                }
                // string to Date
                if (key.endsWith(ExtendMap.MARK_GE)) {
                    // 大于等于
                    AssertUtils.isTrue(ObjectUtil.isNotEmpty(value), CommonErrorCodeEnum.UNKNOWN);
                    Field field = getFieldWithSuffix(key, ExtendMap.MARK_GE);
                    checkUse(dataObj, field.getName());
                    wrapper.ge(calculateDbColumnName(field.getName()),
                            getConversionService().convert(value, field.getType()));
                }
                if (key.endsWith(ExtendMap.MARK_GT)) {
                    // 大于
                    AssertUtils.isTrue(ObjectUtil.isNotEmpty(value), CommonErrorCodeEnum.UNKNOWN);
                    Field field = getFieldWithSuffix(key, ExtendMap.MARK_GT);
                    checkUse(dataObj, field.getName());
                    wrapper.gt(calculateDbColumnName(field.getName()),
                            getConversionService().convert(value, field.getType()));
                }
                if (key.endsWith(ExtendMap.MARK_LE)) {
                    // 小于等于
                    AssertUtils.isTrue(ObjectUtil.isNotEmpty(value), CommonErrorCodeEnum.UNKNOWN);
                    Field field = getFieldWithSuffix(key, ExtendMap.MARK_LE);
                    checkUse(dataObj, field.getName());
                    wrapper.le(calculateDbColumnName(field.getName()),
                            getConversionService().convert(value, field.getType()));
                }
                if (key.endsWith(ExtendMap.MARK_LIKE_RIGHT)) {
                    // 右边加 %
                    AssertUtils.isTrue(ObjectUtil.isNotEmpty(value), CommonErrorCodeEnum.UNKNOWN);
                    Field field = getFieldWithSuffix(key, ExtendMap.MARK_LIKE_RIGHT);
                    checkUse(dataObj, field.getName());
                    wrapper.likeRight(calculateDbColumnName(field.getName()),
                            getConversionService().convert(value, field.getType()));
                }
                if (key.endsWith(ExtendMap.MARK_IN)) {
                    // in
                    AssertUtils.isTrue(ObjectUtil.isNotEmpty(value), CommonErrorCodeEnum.UNKNOWN);
                    AssertUtils.isTrue(Collection.class.isAssignableFrom(value.getClass()),
                            CommonErrorCodeEnum.UNKNOWN);
                    Field field = getFieldWithSuffix(key, ExtendMap.MARK_IN);
                    checkUse(dataObj, field.getName());
                    wrapper.in(calculateDbColumnName(field.getName()), (Collection<?>) value);
                }
                if (key.endsWith(ExtendMap.MARK_NOT_IN)) {
                    // in
                    AssertUtils.isTrue(ObjectUtil.isNotEmpty(value), CommonErrorCodeEnum.UNKNOWN);
                    AssertUtils.isTrue(Collection.class.isAssignableFrom(value.getClass()),
                            CommonErrorCodeEnum.UNKNOWN);
                    Field field = getFieldWithSuffix(key, ExtendMap.MARK_NOT_IN);
                    checkUse(dataObj, field.getName());
                    wrapper.notIn(calculateDbColumnName(field.getName()), (Collection<?>) value);
                }
                String last = "";
                if (key.equals(ExtendMap.MARK_LIMIT)) {
                    // limit
                    AssertUtils.isTrue(ObjectUtil.isNotEmpty(value), CommonErrorCodeEnum.UNKNOWN);
                    last = SQL_LIMIT + (int) value;
                }
                if (key.equals(ExtendMap.FOR_UPDATE)) {
                    // for update
                    last += SQL_FOR_UPDATE;
                }
                if (StringUtils.isNotEmpty(last)) {
                    withLast(wrapper, last);
                }
            }
        }
        LambdaQueryWrapper<ENTITY> lambdaQueryWrapper = wrapper.lambda();
        if (CollectionUtils.isNotEmpty(briefFields)) {
            // 只查部分字段
            //SFunction<ENTITY, ?>[] briefFieldArr = (SFunction<ENTITY, ?>[])Array.newInstance(SFunction.class, briefFields.size());
            // briefFields.stream().toArray();
            lambdaQueryWrapper.select((SFunction<ENTITY, ?>[]) (briefFields.toArray(new SFunction<?, ?>[0])));
        }
        return lambdaQueryWrapper;
    }*/

    /**
     * 检查使用
     * 特殊查询条件字段不能出现在等值查询条件中
     *
     * @param dataObj 等值查询条件
     * @param name    特殊查询条件字段
     */
    private void checkUse(@Nullable ENTITY dataObj, String name) {
        if (dataObj == null) {
            return;
        }
        Object value = ReflectUtil.getFieldValue(dataObj, name);
        if (value != null) {
            throw new IllegalArgumentException("Illegal caller! The field(" + name
                    + ") has 'eq conditional' and 'ext condition', check the code.");
        }
    }

    /**
     * 获取字段，去掉扩展后缀
     *
     * @param key    key：field + suffix
     * @param suffix 扩展后缀
     * @return Field
     */
    protected Field getFieldWithSuffix(String key, String suffix) {
        String dataObjFieldName = StringUtils.substringBeforeLast(key, suffix);
        Field fieldType = ReflectionUtils.findField(getEntityClass(), dataObjFieldName);
        if (fieldType == null) {
            throw new IllegalArgumentException("no such field: " + dataObjFieldName);
        }
        return fieldType;
    }

    /**
     * 只查摘要信息时查哪些字段
     *
     * @return 字段列表
     */
    protected List<SFunction<ENTITY, ?>> getBriefSelectFields(@Nullable Object briefType) {
        return new ArrayList<>(0);
    }

    /**
     * 根据 dataObj 字段 拿到数据库字段
     *
     * @param fieldName dataObj 字段名
     * @return 数据库字段名
     */
    protected String calculateDbColumnName(String fieldName) {
        Field field = ReflectionUtils.findField(getEntityClass(), fieldName);
        if (field == null) {
            throw new IllegalArgumentException("no such field: " + fieldName);
        }
        TableField tf = field.getAnnotation(TableField.class);
        if (tf != null && StringUtils.isNotEmpty(tf.value())) {
            return tf.value();
        }
        if ("id".equals(fieldName)) {
            return "id";
        }
        throw new IllegalArgumentException("no such field: " + fieldName);
    }


    /**
     * 覆盖了父类方法，有问题直接抛异常，对于 bizEntity 默认根据 bizId 更新
     * 注意需要在调用该方法处加锁，判断是否存在
     *
     * @param entity e
     * @return b
     */
    @Override
    public boolean save(ENTITY entity) {
        return super.save(entity);
    }

    /**
     * 透传
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAllById(ENTITY entity) {
        return SqlHelper.retBool(getBaseMapper().updateAllFieldsById(entity));
    }

    @SuppressWarnings("rawtypes")
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdate(ENTITY entity) {
        if (entity instanceof BizEntity) {
            String bizId = ((BizEntity) entity).getBizId();
            if (bizId == null) {
                // 补充 bizId？，默认抛异常
                throw new IllegalStateException("bizId == null");
            }
            return lockByBizId(bizId) != null ? updateByBizId(entity) : save(entity);
        } else {
            return super.saveOrUpdate(entity);
        }
    }

    public <T> T getFirstOrNull(List<T> list) {
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    /**
     * 复制非空的字段，对象类采取合并的方式
     *
     * @param source 源，一般是接口传入
     * @param target 目标，一般是数据库的已有的
     * @param <T>    范型
     */
    @SuppressWarnings("unchecked")
    static <T> void copyNotEmptyProperties(T source, T target) {
        try {
            Class<T> extendClass = (Class<T>) source.getClass();
            Field[] fields = extendClass.getDeclaredFields();
            for (Field field : fields) {
                int modifiers = field.getModifiers();
                if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
                    // final static
                    continue;
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Object newValue = field.get(source);
                if (ObjectUtil.isEmpty(newValue)) {
                    // 空值
                    continue;
                }
                Class<?> fieldType = field.getType();
                if (fieldType.isPrimitive() || CharSequence.class.isAssignableFrom(fieldType) || Number.class.isAssignableFrom(fieldType)
                        || Collection.class.isAssignableFrom(fieldType) || Map.class.isAssignableFrom(fieldType)
                        || fieldType.isArray() || fieldType.isEnum()
                ) {
                    // 基础类型
                    field.set(target, newValue);
                }
                Object targetFieldVal = field.get(target);
                if (fieldType.getName().startsWith("java.")) {
                    // 基础 object 类型
                    field.set(target, newValue);
                } else {
                    // 业务定义对象类型，如扩展字段：内部递归，避免覆盖已有的数据
                    copyNotEmptyProperties(newValue, targetFieldVal);
                }
            }
        } catch (Exception e) {
            throw new BaseRuntimeException(CommonErrorCodeEnum.UNKNOWN, "copy properties FAIL", e);
        }
    }
}
