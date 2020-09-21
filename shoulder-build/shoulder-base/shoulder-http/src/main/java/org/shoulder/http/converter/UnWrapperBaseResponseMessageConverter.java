package org.shoulder.http.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.shoulder.core.dto.response.BaseResponse;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 去除响应中 {@link BaseResponse} 的包装
 *
 * @author lym
 */
public class UnWrapperBaseResponseMessageConverter extends MappingJackson2HttpMessageConverter {

    public UnWrapperBaseResponseMessageConverter() {
        super();
    }

    public UnWrapperBaseResponseMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @NonNull
    @Override
    public Object read(@NonNull Type type, @Nullable Class<?> contextClass, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {

        Object result = super.read(type, contextClass, inputMessage);
        Object toCrypt = result;
        // 专门处理 BaseResponse 以及其子类
        if (result instanceof BaseResponse) {
            toCrypt = ((BaseResponse) toCrypt).getData();
        }
        if (toCrypt == null) {
            return result;
        }
        Class<?> resultClazz = toCrypt.getClass();
        return result;
    }


}
