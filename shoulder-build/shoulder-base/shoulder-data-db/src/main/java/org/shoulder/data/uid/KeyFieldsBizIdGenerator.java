package org.shoulder.data.uid;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ReflectUtil;
import jakarta.annotation.Nonnull;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.constant.ByteSpecification;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.data.annotation.BizIdSource;
import org.shoulder.data.mybatis.template.entity.BizEntity;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * bizId 生成算法 —— 将所有标志性字段相加并 hash
 *
 * @author lym
 */
@SuppressWarnings("unchecked,rawtypes")
public class KeyFieldsBizIdGenerator implements ConditionalBizIdGenerator {

    private final String fieldValueSplit;

    public KeyFieldsBizIdGenerator(String fieldValueSplit) {
        this.fieldValueSplit = fieldValueSplit;
    }

    @Override
    public String generateBizId(BizEntity entity, Class<? extends BizEntity> entityClass) {
        if (!support(entity, entityClass)) {
            throw new IllegalArgumentException("not support such entity " + entityClass.getName());
        }
        List<Field> keyFields = getAllKeyFields(entityClass);
        List<Object> values = calculateSource(entity, keyFields);
        return genBizIdFromSource(values);
    }

    public boolean support(BizEntity entity, Class<? extends BizEntity> entityClass) {
        // 类里有字段标注了 @BizIdSource
        return CollectionUtils.isNotEmpty(getAllKeyFields(entityClass));
    }

    @Nonnull
    protected List<Field> getAllKeyFields(Class<? extends BizEntity> entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        return Arrays.stream(fields)
                .filter(f -> !Modifier.isFinal(f.getModifiers()) && !Modifier.isStatic(f.getModifiers()))
                .filter(f -> AnnotationUtil.hasAnnotation(f, BizIdSource.class))
                .collect(Collectors.toList());
    }

    @Nonnull
    protected List<Object> calculateSource(@Nonnull BizEntity<? extends Serializable> entity,
                                           @Nonnull List<Field> fields) {
        AssertUtils.notEmpty(fields, CommonErrorCodeEnum.CODING, "bizId generate Fail: [" + entity.getClass()
                                                                 + "] no fields annotated with @BizIdSource, please check code or input bizId!");
        return fields.stream()
                .map(f -> ReflectUtil.getFieldValue(entity, f))
                .collect(Collectors.toList());
    }

    /**
     * 生成 bizId
     * 默认通过 hash 算法将元字段 hash
     *
     * @param sourceFieldValues 源字段
     * @return bizId
     * @implSpec 该方法必定幂等
     */
    @Nonnull
    protected String genBizIdFromSource(List<Object> sourceFieldValues) {
        StringJoiner sj = new StringJoiner(fieldValueSplit);
        for (Object sourceFieldValue : sourceFieldValues) {
            sj.add(String.valueOf(sourceFieldValue));
        }
        // md5
        return Md5Crypt.md5Crypt(sj.toString().getBytes(ByteSpecification.STD_CHAR_SET));
    }

}
