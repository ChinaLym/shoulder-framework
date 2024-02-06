package org.shoulder.ext.config.domain;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.ext.common.constant.ShoulderExtConstants;
import org.shoulder.ext.config.domain.enums.ConfigErrorCodeEnum;
import org.shoulder.ext.config.domain.ex.ConfigException;
import org.shoulder.ext.config.domain.model.ConfigFieldInfo;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 *
 * @author lym
 */
@Data
public class ConfigTypeInfo implements ConfigType {

    private final String configName;

    private final String description;

    private final Class<?> clazz;

    private final List<ConfigFieldInfo> fieldInfoList;

    private final List<ConfigFieldInfo> indexFieldInfoList;

    public ConfigTypeInfo(Class<?> clazz, String description) {
        this.configName = clazz.getSimpleName();
        this.description = description;
        this.clazz = clazz;
        this.fieldInfoList = resolveFieldInfo(clazz);
        this.indexFieldInfoList = fieldInfoList.stream()
                // 索引字段
                .filter(ConfigFieldInfo::isIndex)
                // 主动按名称排序，避免字段顺序修改导致问题
                .sorted(Comparator.comparing(ConfigFieldInfo::getName))
                .collect(Collectors.toList());
        // 不能为空
        AssertUtils.notEmpty(indexFieldInfoList, ConfigErrorCodeEnum.CODING_ERROR, clazz.getName(), "must at least one field annotated @ConfigField(index=true)!");
    }

    // ======================================== static 解析字段 ============================================

    /**
     * 解析类字段
     * 以 JSR 标准注解为主，支持部分 hibernate 定义的注解
     *
     * @param configClazz 配置类
     * @return 字段信息
     */
    private static List<ConfigFieldInfo> resolveFieldInfo(Class<?> configClazz) {
        Field[] fields = configClazz.getDeclaredFields();
        List<ConfigFieldInfo> fieldInfoList = new ArrayList<>(fields.length);
        int order = 0;
        for (Field field : fields) {

            int modifiers = field.getModifiers();
            if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers)) {
                continue;
            }

            try {
                ConfigFieldInfo fieldInfo = new ConfigFieldInfo();
                fieldInfo.setOrderNum(order);
                // 解析基础字段信息
                parseCommon(field, fieldInfo);

                // 针对字段类型进行解析
                BiConsumer<Field, ConfigFieldInfo> fieldInfoParse;
                if (CharSequence.class.isAssignableFrom(field.getType())) {
                    fieldInfoParse = ConfigTypeInfo::parseCharSequence;
                } else if (Number.class.isAssignableFrom(field.getType())) {
                    fieldInfoParse = ConfigTypeInfo::parseNumber;
                } else if (Boolean.class.isAssignableFrom(field.getType())) {
                    fieldInfoParse = ConfigTypeInfo::parseBoolean;
                } else {
                    fieldInfoParse = ConfigTypeInfo::parseOthers;
                }
                fieldInfoParse.accept(field, fieldInfo);

                formatDescription(fieldInfo);

                // getter / setter 方法
                resolveGetterSetter(configClazz, field, fieldInfo);

                fieldInfoList.add(fieldInfo);
                // 成功解析序号才增
                order++;
            } catch (Exception e) {
                LoggerFactory.getLogger(ShoulderExtConstants.BACKSTAGE_BIZ_SERVICE_LOGGER).error(
                        "parse '" + configClazz.getTypeName() + "' FAIL!", e);
                throw new ConfigException(e, CommonErrorCodeEnum.UNKNOWN);
            }
        }
        return fieldInfoList;
    }

    private static void formatDescription(ConfigFieldInfo fieldInfo) {

        StringBuilder sb = new StringBuilder();
        if (fieldInfo.isNotNull()) {
            sb.append("必填！ ");
        } else {
            sb.append("可选 ");
        }
        String type = fieldInfo.getType().getSimpleName();
        sb.append(type).append(" ");
        if (StringUtils.isNotBlank(fieldInfo.getDescription())) {
            sb.append(fieldInfo.getDescription()).append(" ");
        }
        if (fieldInfo.getMinLength() > 0) {
            sb.append("最小长度 ").append(fieldInfo.getMinLength()).append(" ");
        }
        if (fieldInfo.getMaxLength() < 65536) {
            sb.append("最大长度 ").append(fieldInfo.getMaxLength()).append(" ");
        }
        if (StringUtils.isNotEmpty(fieldInfo.getRegex())) {
            sb.append("满足正则 ").append(fieldInfo.getRegex()).append(" ");
        }
        if (Number.class.isAssignableFrom(fieldInfo.getType())) {
            if (fieldInfo.getMin() > Integer.MIN_VALUE) {
                sb.append("最小值为 ").append(fieldInfo.getMin()).append(" ");
            }
            if (fieldInfo.getMax() < Integer.MAX_VALUE) {
                sb.append("最大值为 ").append(fieldInfo.getMax()).append(" ");
            }
        }
        fieldInfo.setDescription(sb.toString());
    }

    private static void resolveGetterSetter(Class<?> configClazz, Field field, ConfigFieldInfo fieldInfo) throws NoSuchMethodException {
        String fieldName = field.getName();
        String upperLeadingCharName = capitalize(fieldName);
        if (field.getType() == boolean.class) {
            if (fieldName.startsWith("is")) {
                fieldInfo.setReadMethod(configClazz.getMethod(fieldName));
                fieldInfo.setWriteMethod(configClazz.getMethod("set" + capitalize(fieldName.substring(2)), field.getType()));
            } else {
                fieldInfo.setReadMethod(configClazz.getMethod("is" + upperLeadingCharName));
                fieldInfo.setWriteMethod(configClazz.getMethod("set" + upperLeadingCharName, field.getType()));
            }
        } else {
            fieldInfo.setReadMethod(configClazz.getMethod("get" + upperLeadingCharName));
            fieldInfo.setWriteMethod(configClazz.getMethod("set" + upperLeadingCharName, field.getType()));
        }
    }

    /**
     * 解析基础字段信息
     */
    private static void parseCommon(Field field, ConfigFieldInfo fieldInfo) {
        fieldInfo.setType(field.getType());
        fieldInfo.setIndex(false);
        fieldInfo.setName(field.getName());
        fieldInfo.setDisplayName(field.getName());
        fieldInfo.setDescription("");
        fieldInfo.setNotNull(AnnotationUtils.findAnnotation(field, NotNull.class) != null);
        ConfigField fieldAnnotation = AnnotationUtils.findAnnotation(field, ConfigField.class);
        if (fieldAnnotation != null) {
            // 存在注解，增强处理
            if (fieldAnnotation.indexKey()) {
                fieldInfo.setIndex(true);
                // 断言索引字段上必须有 @NotNull 或其衍生注解（如 @NotEmpty/@NotBlank），否则单测不通过，同时启动失败
                AssertUtils.isTrue(AnnotationUtils.findAnnotation(field, NotNull.class) != null,
                        ConfigErrorCodeEnum.CODING_ERROR,
                        field.toString(), "need @NotNull on all indexField!");
            }
            // 别名
            if (StringUtils.isNotBlank(fieldAnnotation.name())) {
                fieldInfo.setName(fieldAnnotation.name());
            }
            if (StringUtils.isNotBlank(fieldAnnotation.chineseName())) {
                fieldInfo.setDisplayName(fieldAnnotation.chineseName());
            }
            fieldInfo.setDescription(fieldAnnotation.description());
        }
    }

    private static void parseCharSequence(Field field, ConfigFieldInfo fieldInfo) {
        assert CharSequence.class.isAssignableFrom(field.getType());
        boolean notBlank = AnnotationUtils.findAnnotation(field, NotBlank.class) != null;
        if (notBlank) {
            fieldInfo.setNotNull(true);
            fieldInfo.setNotBlank(true);
        }
        Length length = AnnotationUtils.findAnnotation(field, Length.class);
        if (length != null) {
            fieldInfo.setMinLength(length.min());
            fieldInfo.setMaxLength(length.max());
        } else {
            Size size = AnnotationUtils.findAnnotation(field, Size.class);
            if (size != null) {
                fieldInfo.setMinLength(size.min());
                fieldInfo.setMaxLength(size.max());
            }
        }
        Pattern pattern = AnnotationUtils.findAnnotation(field, Pattern.class);
        if (pattern != null) {
            fieldInfo.setRegex(pattern.regexp());
        }
    }

    private static void parseNumber(Field field, ConfigFieldInfo fieldInfo) {
        assert Number.class.isAssignableFrom(field.getType());
        Range range = AnnotationUtils.findAnnotation(field, Range.class);
        if (range != null) {
            fieldInfo.setMax(range.max());
            fieldInfo.setMin(range.min());
        } else {
            Max max = AnnotationUtils.findAnnotation(field, Max.class);
            if (max != null) {
                fieldInfo.setMax(max.value());
            }
            Min min = AnnotationUtils.findAnnotation(field, Min.class);
            if (min != null) {
                fieldInfo.setMin(min.value());
            }
        }
    }

    private static void parseBoolean(Field field, ConfigFieldInfo fieldInfo) {
        assert Boolean.class.isAssignableFrom(field.getType());
    }

    private static void parseOthers(Field field, ConfigFieldInfo fieldInfo) {
        // 复杂类型，暗示使用 json
    }

    /**
     * 首字母大写。
     *
     * @param propertyName 属性值
     * @return 大写的首字母
     */
    private static String capitalize(String propertyName) {
        if (StringUtils.isBlank(propertyName)) {
            return propertyName;
        }
        return propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
    }


}
