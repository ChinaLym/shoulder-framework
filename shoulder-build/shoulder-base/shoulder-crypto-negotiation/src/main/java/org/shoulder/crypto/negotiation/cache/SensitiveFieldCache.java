package org.shoulder.crypto.negotiation.cache;

import cn.hutool.core.util.ReflectUtil;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.crypto.negotiation.annotation.Sensitive;
import org.shoulder.crypto.negotiation.cache.cipher.TransportCipher;
import org.shoulder.crypto.negotiation.cache.dto.SensitiveFieldWrapper;
import org.springframework.lang.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 敏感字段信息緩存
 *
 * @author lym
 */
public class SensitiveFieldCache {

    private static final ConcurrentMap<Class<?>, List<SensitiveFieldWrapper>> REQUEST_FIELD_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Class<?>, List<SensitiveFieldWrapper>> RESPONSE_FIELD_CACHE = new ConcurrentHashMap<>();

    public static List<SensitiveFieldWrapper> findSensitiveRequestFieldInfo(@NonNull Class<?> clazz) {
        return REQUEST_FIELD_CACHE.computeIfAbsent(clazz,
            clz -> findSensitiveFields(clz, true)
        );
    }

    public static List<SensitiveFieldWrapper> findSensitiveResponseFieldInfo(@NonNull Class<?> clazz) {
        return RESPONSE_FIELD_CACHE.computeIfAbsent(clazz,
            clz -> findSensitiveFields(clz, false)
        );
    }

    /**
     * 反射找该类的所有敏感字段，包括父类，注意 getFields getDeclaredFields 都不行，需要递归父类
     */
    private static List<SensitiveFieldWrapper> findSensitiveFields(@NonNull Class<?> aimClazz, boolean requestOrResponse) {

        List<SensitiveFieldWrapper> allSensitiveField = new LinkedList<>();
        Field[] fields = ReflectUtil.getFieldsDirectly(aimClazz, true);
        SensitiveFieldWrapper wrapper;
        for (Field field : fields) {
            Sensitive requestSecret = field.getAnnotation(Sensitive.class);
            if (requestSecret == null) {
                if (field.getClass().isAssignableFrom(Object.class)) {
                    // todo 处理特殊类：BaseResponse 这种带泛型的
                    System.out.println("");//to debug
                }
                continue;
            }
            boolean include = requestOrResponse ? requestSecret.sensitiveRequest() : requestSecret.sensitiveResponse();
            if (!include) {
                continue;
            }
            wrapper = new SensitiveFieldWrapper(field);
            Class<?> fieldClass;
            if (!String.class.isAssignableFrom(fieldClass = field.getType())) {
                // 如果是复杂变量还需要递归，可以通过加类注解减少递归复杂度，否则报错/警告，用法错误
                allSensitiveField.addAll(findSensitiveFields(fieldClass, requestOrResponse));
            }
            allSensitiveField.add(wrapper);
            // 压缩内存、校准状态
            wrapper.clearedUp();
        }
        // 压缩内存
        return new ArrayList<>(allSensitiveField);
    }

    /**
     * 处理敏感字段
     *
     * @param object          obj
     * @param sensitiveFields 所有需要处理的敏感字段信息
     * @param encrypt         加密/解密
     */
    public static void handleSensitiveData(@NonNull Object object, @NonNull List<SensitiveFieldWrapper> sensitiveFields,
                                           @NonNull TransportCipher cipher) {
        try {
            // 不应该为空
            assert cipher != null;
            for (SensitiveFieldWrapper filedWrapper : sensitiveFields) {
                Field field = filedWrapper.getField();
                if (filedWrapper.isSensitive()) {
                    field.setAccessible(true);
                    // 意味着一定是 String 类型
                    String origin = (String) field.get(object);
                    String handled = cipher.doCipher(origin);
                    field.set(object, handled);
                } else {
                    handleSensitiveData(field.get(object), filedWrapper.getInternalFields(), cipher);
                }

            }
        } catch (Exception e) {
            throw new BaseRuntimeException("doCipher fail!", e);
        }
    }

    /**
     * java反射bean的get/set方法
     *
     * @param objectClass objectClass
     * @param fieldName   fieldName
     * @param get         get/set
     * @return Method
     */
   /* public static Method findGetSetMethod(Class<?> objectClass, Field field, boolean get) {
        // 某些特殊的 非驼峰命名字段可能有问题？
        String fieldName = field.getName();
        String methodName = (get ? "get" : "set" ) +
            fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        try {
            return objectClass.getMethod(methodName, field.getType());
        } catch (Exception e) {
            throw new RuntimeException("can't find " + fieldName + "'s get/set method(" + methodName + ") for " +
                objectClass.getName(), e);
        }
    }*/


}
