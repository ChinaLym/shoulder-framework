package org.shoulder.crypto.negotiation.support.client;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.util.JsonUtils;
import org.shoulder.crypto.negotiation.cache.TransportCipherHolder;
import org.shoulder.crypto.negotiation.cipher.TransportTextCipher;
import org.shoulder.crypto.negotiation.dto.SensitiveFieldWrapper;
import org.shoulder.crypto.negotiation.support.SecurityRestTemplate;
import org.shoulder.crypto.negotiation.util.SensitiveFieldCache;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 秘密传输转换器（已经进行过密钥交换，{@link SecurityRestTemplate#httpEntityCallback}）
 *
 * @author lym
 */
public class SensitiveRequestEncryptMessageConverter extends MappingJackson2HttpMessageConverter {

    public SensitiveRequestEncryptMessageConverter() {
        super();
    }

    public SensitiveRequestEncryptMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    /**
     * HttpClient 发送前，序列化前加密
     */
    @Override
    protected void writeInternal(Object object, @Nullable Type type, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {
        // 获取参数类型，
        if (object instanceof MappingJacksonValue container) {
            object = container.getValue();
        }
        // 提前取出并清理，避免遗漏
        TransportTextCipher requestEncryptCipher = TransportCipherHolder.removeRequestCipher();
        Class<?> objectClass = object.getClass();
        List<SensitiveFieldWrapper> securityParamField = SensitiveFieldCache.findSensitiveRequestFieldInfo(objectClass);

        if (CollectionUtils.isNotEmpty(securityParamField)) {
            // 参数需要加密，加密处理器必定不是 null
            Assert.notNull(requestEncryptCipher, "requestEncryptCipher must not be null " +
                "when request param with sensitive fields");
            // todo 【性能】这里深克隆了所有属性，应该只保存需要加密的字段，以提高性能
            Object cloned = ObjectUtil.clone(object);
            if (cloned == null) {
                //使用 json 方式（性能差一点，备选）
                cloned = JsonUtils.parseObject(JsonUtils.toJson(object), objectClass);
            }
            object = cloned;

            // 加密敏感数据
            SensitiveFieldCache.handleSensitiveData(object, securityParamField, requestEncryptCipher);
        }

        // 序列化
        super.writeInternal(object, type, outputMessage);
    }

    /**
     * HttpClient 接收前，反序列化 + 解密
     */
    @Override
    public Object read(Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {

        Object result = super.read(type, contextClass, inputMessage);
        Object toCrypt = result;
        // 专门处理 RestResult 以及其子类
        if (result instanceof BaseResult) {
            toCrypt = ((BaseResult) toCrypt).getData();
        }
        if (toCrypt == null) {
            return result;
        }
        Class<?> resultClazz = toCrypt.getClass();
        TransportTextCipher cipher = TransportCipherHolder.removeResponseCipher();
        List<SensitiveFieldWrapper> securityResultField = SensitiveFieldCache.findSensitiveResponseFieldInfo(resultClazz);
        if (CollectionUtils.isNotEmpty(securityResultField)) {
            // 解密
            SensitiveFieldCache.handleSensitiveData(toCrypt, securityResultField, cipher);
        }
        return result;
    }


}
