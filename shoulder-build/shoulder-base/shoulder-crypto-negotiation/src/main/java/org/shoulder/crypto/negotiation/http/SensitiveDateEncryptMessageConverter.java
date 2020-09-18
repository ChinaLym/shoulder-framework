package org.shoulder.crypto.negotiation.http;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.negotiation.annotation.RequestSecret;
import org.shoulder.crypto.negotiation.annotation.ResponseSecret;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lym
 */
public class SensitiveDateEncryptMessageConverter extends MappingJackson2HttpMessageConverter {

    private final ConcurrentMap<Class<?>, List<Field>> securityParamFieldCache = new ConcurrentHashMap<>();

    private final ConcurrentMap<Class<?>, List<Field>> securityResponseFieldCache = new ConcurrentHashMap<>();

    public SensitiveDateEncryptMessageConverter() {
        super();
    }

    public SensitiveDateEncryptMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    /**
     * getFields()	获取所有public字段,包括父类字段
     * getDeclaredFields()	获取所有字段,public和protected和private,但是不包括父类字段
     * 递归Model的父类去getDeclaredFields()
     *
     * @param object
     * @param type
     * @param outputMessage
     * @throws IOException
     * @throws HttpMessageNotWritableException
     */
    @Override
    protected void writeInternal(@NonNull Object object, @Nullable Type type, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        // 先加密
        Object param = object;
        if (object instanceof MappingJacksonValue) {
            MappingJacksonValue container = (MappingJacksonValue) object;
            param = container.getValue();
        }
        Class<?> paramClazz = object.getClass();
        List<Field> securityParamField = getRequestFields(paramClazz);
        // todo 确保已经进行过密钥交换
        if (!CollectionUtils.isEmpty(securityParamField)) {
            // 参数需要加密
            // todo 深克隆所有属性
            Object cloned = ObjectUtil.clone(object);
            if (cloned == null) {
                //使用 json 方式（性能差一点，备选）
                cloned = JsonUtils.toObject(JsonUtils.toJson(object), paramClazz);
            }
            object = cloned;

            // 加密敏感数据
            encryptSensitiveData(object, securityParamField);
        }

        // 序列化
        super.writeInternal(object, type, outputMessage);
    }


    private void encryptSensitiveData(Object object, List<Field> sensitiveFields) {
        dellSensitiveData(object, sensitiveFields, true);
    }

    private void decryptSensitiveData(Object object, List<Field> sensitiveFields) {
        dellSensitiveData(object, sensitiveFields, false);
    }

    /**
     * 处理敏感字段
     *
     * @param object          obj
     * @param sensitiveFields 所有需要处理的敏感字段信息
     * @param encrypt         加密/解密
     */
    private void dellSensitiveData(Object object, List<Field> sensitiveFields, boolean encrypt) {
        try {
            for (Field filed : sensitiveFields) {
                filed.setAccessible(true);
                // todo 支持 String/byte[] 类型
                String origin = (String) filed.get(object);
                // todo encrypt
                String handled = encrypt ? "xxx" : "000";
                filed.set(object, handled);
            }
        } catch (Exception e) {
            throw new BaseRuntimeException((encrypt ? "encrypt" : "decrypt") + " fail!", e);
        }
    }

    @NonNull
    @Override
    public Object read(@NonNull Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {

        Object result = super.read(type, contextClass, inputMessage);
        Class<?> resultClazz = result.getClass();
        List<Field> securityResultField = getRequestFields(resultClazz);
        if (!CollectionUtils.isEmpty(securityResultField)) {
            // 解密
            decryptSensitiveData(result, securityResultField);
        }
        return result;
    }

    private List<Field> getRequestFields(Class<?> aimClazz) {
        return securityParamFieldCache.computeIfAbsent(aimClazz, clazz -> {
            List<Field> requestSecretFields = new LinkedList<>();
            List<Field> responseSecretFields = new LinkedList<>();
            findSensitiveFields(aimClazz, requestSecretFields, responseSecretFields);
            securityResponseFieldCache.putIfAbsent(clazz, CollectionUtils.isEmpty(responseSecretFields) ?
                Collections.emptyList() : responseSecretFields);
            return requestSecretFields;
        });
    }

    private List<Field> getResponseFields(Class<?> aimClazz) {
        return securityResponseFieldCache.computeIfAbsent(aimClazz, clazz -> {
            List<Field> requestSecretFields = new LinkedList<>();
            List<Field> responseSecretFields = new LinkedList<>();
            findSensitiveFields(aimClazz, requestSecretFields, responseSecretFields);
            securityParamFieldCache.putIfAbsent(clazz, CollectionUtils.isEmpty(requestSecretFields) ?
                Collections.emptyList() : requestSecretFields);
            return responseSecretFields;
        });
    }

    private void findSensitiveFields(Class<?> aimClazz, List<Field> requestSecretFields, List<Field> responseSecretFields) {
// 反射找该类的所有敏感字段，包括父类

        Field[] fields = ReflectUtil.getFieldsDirectly(aimClazz, true);
        // 寻找敏感字段
        for (Field field : fields) {
            RequestSecret requestSecret = field.getAnnotation(RequestSecret.class);
            ResponseSecret responseSecret = field.getAnnotation(ResponseSecret.class);
            if (requestSecret != null) {
                requestSecretFields.add(field);
            }
            if (responseSecret != null) {
                responseSecretFields.add(field);
            }
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
