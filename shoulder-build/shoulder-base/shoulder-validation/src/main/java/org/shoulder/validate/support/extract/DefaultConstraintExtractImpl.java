package org.shoulder.validate.support.extract;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Nonnull;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.ArrayUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.validate.support.dto.ConstraintInfoDTO;
import org.shoulder.validate.support.dto.FieldValidationRuleDTO;
import org.shoulder.validate.support.mateconstraint.ConstraintConverter;
import org.shoulder.validate.support.model.ValidConstraint;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认约束提取器
 * 当且实现依赖 hibernate 中的实现类 for JSR 的无法获取到 字段所在类信息
 *
 * @author lym
 */
public class DefaultConstraintExtractImpl implements ConstraintExtract {

    public static final Logger log = LoggerFactory.getLogger(ConstraintExtract.class);

    /**
     * 字段校验信息缓存
     */
    private final Map<String, Map<String, FieldValidationRuleDTO>> CACHE = new HashMap<>();

    /**
     * 校验注解提取
     */
    private BeanMetaDataManager beanMetaDataManager;

    /**
     * annotation 转换为 dto
     */
    private final List<ConstraintConverter> constraintConverters;

    public DefaultConstraintExtractImpl(Validator validator, List<ConstraintConverter> constraintConverters) {
        this.constraintConverters = constraintConverters;
        try {
            if (validator instanceof SpringValidatorAdapter) {
                Field targetValidatorField = SpringValidatorAdapter.class.getDeclaredField("targetValidator");
                targetValidatorField.setAccessible(true);
                validator = (Validator) targetValidatorField.get(validator);
            }
            assert validator instanceof ValidatorImpl;
            Field beanMetaDataManagerField = ValidatorImpl.class.getDeclaredField("beanMetaDataManager");
            beanMetaDataManagerField.setAccessible(true);
            beanMetaDataManager = (BeanMetaDataManager) beanMetaDataManagerField.get(validator);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("初始化验证器失败", e);
        }
    }


    @Override
    public List<FieldValidationRuleDTO> extract(@Nonnull List<ValidConstraint> constraintsOnMethod) throws Exception {
        if (CollectionUtils.isEmpty(constraintsOnMethod)) {
            return Collections.emptyList();
        }
        Map<String, FieldValidationRuleDTO> fieldValidatorDesc = new HashMap<>(constraintsOnMethod.size());
        for (ValidConstraint constraint : constraintsOnMethod) {
            doExtract(constraint, fieldValidatorDesc);
        }
        return new ArrayList<>(fieldValidatorDesc.values());
    }


    private void doExtract(ValidConstraint constraintOnMethod, Map<String, FieldValidationRuleDTO> fieldValidatorDesc) throws Exception {
        Class<?> targetMethodClazz = constraintOnMethod.getTarget();
        Class<?>[] groupsOnMethod = constraintOnMethod.getGroups();

        // clazz:groupsOnMethod
        String key = targetMethodClazz.getName() + StrPool.COLON +
                Arrays.stream(groupsOnMethod).map(Class::getName).collect(Collectors.joining(StrPool.COLON));

        Map<String, FieldValidationRuleDTO> cache = CACHE.get(key);
        if (cache != null) {
            fieldValidatorDesc.putAll(cache);
            return;
        }

        // JSR 标准中获取不到字段所在类信息，故先使用 hibernate 中的
        //validator.getConstraintsForClass(targetMethodClazz).getConstrainedProperties()

        BeanMetaData<?> beanMetaData = beanMetaDataManager.getBeanMetaData(targetMethodClazz);
        Set<MetaConstraint<?>> beanMetaConstraints = beanMetaData.getMetaConstraints();
        for (MetaConstraint<?> beanMetaConstraint : beanMetaConstraints) {
            // 这里认为 DTO 都是展平的，只获取一层，不再递归处理复杂类型字段
            builderFieldValidatorDesc(beanMetaConstraint, groupsOnMethod, fieldValidatorDesc);
        }
        // 字段自身：notNull 等；如果是基本类型，则还可能有基础校验注解

        FieldValidationRuleDTO ruleDTO = new FieldValidationRuleDTO();
        ruleDTO.setField("#self");
        ruleDTO.setFieldType(convertToJsType(constraintOnMethod.getTarget().getName()));
        ruleDTO.setConstraints(new ArrayList<>());
        if (ArrayUtils.isNotEmpty(constraintOnMethod.getMethodAnnotations())) {
            for (Annotation methodAnnotation : constraintOnMethod.getMethodAnnotations()) {
                ConstraintInfoDTO constraint = buildConstraintInfo(methodAnnotation);
                if (constraint != null) {
                    ruleDTO.getConstraints().add(constraint);
                }
            }
        }
        fieldValidatorDesc.put("#self", ruleDTO);
        CACHE.put(key, fieldValidatorDesc);
    }


    /**
     * 创建字段校验描述信息
     *
     * @param beanMetaConstraint 什么注解
     * @param groupsOnMethod     所在的校验组
     * @param fieldValidatorDesc 字段描述信息，传入用于避免重复获取
     * @throws Exception ex
     */
    private void builderFieldValidatorDesc(MetaConstraint<?> beanMetaConstraint, Class<?>[] groupsOnMethod,
                                           Map<String, FieldValidationRuleDTO> fieldValidatorDesc) throws Exception {
        Set<Class<?>> groupsOnBean = beanMetaConstraint.getGroupList();
        if (isContainsGroup(groupsOnBean, groupsOnMethod)) {
            // 没有激活的校验分组
            return;
        }

        // 获取分组、类名、字段名、字段类型
        ConstraintLocation beanMetaConstraintLocation = beanMetaConstraint.getLocation();
        String beanClassName = beanMetaConstraintLocation.getDeclaringClass().getSimpleName();
        String fieldName = beanMetaConstraintLocation.getConstrainable().getName();
        String fieldKey = beanClassName + fieldName;

        // 字段信息（可能已经创建了）
        FieldValidationRuleDTO ruleDTO = fieldValidatorDesc.get(fieldKey);
        if (ruleDTO == null) {
            ruleDTO = new FieldValidationRuleDTO();
            ruleDTO.setField(fieldName);
            String fieldType = beanMetaConstraintLocation.getConstrainable().getType().getTypeName();
            ruleDTO.setFieldType(convertToJsType(fieldType));
            ruleDTO.setConstraints(new ArrayList<>());
            fieldValidatorDesc.put(fieldKey, ruleDTO);
        }
        // 校验信息（不同校验组）
        ConstraintInfoDTO constraint = buildConstraintInfo(beanMetaConstraint.getDescriptor().getAnnotation());
        if (constraint != null) {
            ruleDTO.getConstraints().add(constraint);
        }

        // 特殊的补充
        if (Pattern.class == beanMetaConstraint.getDescriptor().getAnnotationType()) {
            ConstraintInfoDTO notNull = new ConstraintInfoDTO();
            notNull.setType("NotNull");
            Map<String, Object> attrs = new HashMap<>(1);
            attrs.put("message", "can't be empty");
            notNull.setAttributes(attrs);
            ruleDTO.getConstraints().add(notNull);
        }
    }

    /**
     * 是否有激活的校验组
     *
     * @param groupsOnBean   方法上准备校验的校验组
     * @param groupsOnMethod 这个字段支持的校验组
     * @return groupsOnBean 里有任意一个 groupsOnMethod
     */
    private boolean isContainsGroup(Set<Class<?>> groupsOnBean, Class<?>[] groupsOnMethod) {
        boolean isContainsGroup = false;

        //需要验证的组
        for (Class<?> group : groupsOnMethod) {
            if (groupsOnBean.contains(group)) {
                isContainsGroup = true;
                break;
            }
            for (Class<?> g : groupsOnBean) {
                if (g.isAssignableFrom(group)) {
                    isContainsGroup = true;
                    break;
                }
            }
        }
        return isContainsGroup;
    }

    /**
     * 转成 javaScript 的类型名称
     */
    private String convertToJsType(String typeName) {
        if (StrUtil.startWithAny(typeName, StringUtils.CLASS_NAME_SET, StringUtils.CLASS_NAME_LIST, StringUtils.CLASS_NAME_COLLECTION)) {
            return "Array";
        } else if (StrUtil.equalsAny(typeName, StringUtils.CLASS_NAME_LONG, StringUtils.CLASS_NAME_INTEGER, StringUtils.CLASS_NAME_SHORT)) {
            return "Integer";
        } else if (StrUtil.equalsAny(typeName, StringUtils.CLASS_NAME_DOUBLE, StringUtils.CLASS_NAME_FLOAT)) {
            return "Float";
        } else if (StrUtil.equalsAny(typeName, StringUtils.CLASS_NAME_LOCAL_DATE_TIME, StringUtils.CLASS_NAME_DATE)) {
            return "DateTime";
        } else if (StrUtil.equalsAny(typeName, StringUtils.CLASS_NAME_LOCAL_DATE)) {
            return "Date";
        } else if (StrUtil.equalsAny(typeName, StringUtils.CLASS_NAME_LOCAL_TIME)) {
            return "Time";
        } else if (StrUtil.equalsAny(typeName, StringUtils.CLASS_NAME_BOOLEAN)) {
            return "Boolean";
        }
        return StrUtil.subAfter(typeName, CharUtil.DOT, true);
    }

    /**
     * 将 JSR 校验注解转化为约束信息
     *
     * @param annotation annotation
     * @return null if NOT Support
     * @throws Exception 解析失败
     */
    private ConstraintInfoDTO buildConstraintInfo(Annotation annotation) throws Exception {
        for (ConstraintConverter constraintConverter : constraintConverters) {
            if (constraintConverter.support(annotation.annotationType())) {
                return constraintConverter.converter(annotation);
            }
        }
        // 没有支持该注解的解析器
        log.info(CommonErrorCodeEnum.CODING);
        return null;
    }
}
