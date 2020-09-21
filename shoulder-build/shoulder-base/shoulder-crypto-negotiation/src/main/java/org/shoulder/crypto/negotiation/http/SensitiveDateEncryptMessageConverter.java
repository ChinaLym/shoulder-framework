package org.shoulder.crypto.negotiation.http;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.negotiation.cache.SensitiveFieldCache;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cache.dto.SensitiveFieldWrapper;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 秘密传输转换器（已经进行过密钥交换，{@link SecurityRestTemplate#httpEntityCallback}）
 *
 * @author lym
 */
public class SensitiveDateEncryptMessageConverter extends MappingJackson2HttpMessageConverter {

    public SensitiveDateEncryptMessageConverter() {
        super();
    }

    public SensitiveDateEncryptMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    /**
     * 发送前，序列化前加密
     */
    @Override
    protected void writeInternal(@NonNull Object object, @Nullable Type type, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        // 获取参数类型，
        Object param = object;
        if (object instanceof MappingJacksonValue) {
            MappingJacksonValue container = (MappingJacksonValue) object;
            param = container.getValue();
        }
        Class<?> objectClass = object.getClass();
        List<SensitiveFieldWrapper> securityParamField = SensitiveFieldCache.findSensitiveRequestFieldInfo(objectClass);

        if (!CollectionUtils.isEmpty(securityParamField)) {
            // 参数需要加密
            // todo 这里深克隆了所有属性，应该只保存需要加密的字段，以提高性能
            Object cloned = ObjectUtil.clone(object);
            if (cloned == null) {
                //使用 json 方式（性能差一点，备选）
                cloned = JsonUtils.toObject(JsonUtils.toJson(object), objectClass);
            }
            object = cloned;

            // 加密敏感数据
            SensitiveFieldCache.handleSensitiveData(object, securityParamField, TransportCipherHolder.getRequestCipher());
        }

        // 序列化
        super.writeInternal(object, type, outputMessage);
    }

    /*@NonNull
    @Override
    public Object read(@NonNull Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {

        Object result = super.read(type, contextClass, inputMessage);
        // 专门处理 BaseResponse 以及其子类
        if (result instanceof BaseResponse) {
            result = ((BaseResponse) result).getData();
        }
        Class<?> resultClazz = result.getClass();
        List<SensitiveFieldWrapper> securityResultField = SensitiveFieldCache.findSensitiveResponseFieldInfo(resultClazz);
        if (!CollectionUtils.isEmpty(securityResultField)) {
            // 解密
            decryptSensitiveData(result, securityResultField);
        }
        return result;
    }*/


}
