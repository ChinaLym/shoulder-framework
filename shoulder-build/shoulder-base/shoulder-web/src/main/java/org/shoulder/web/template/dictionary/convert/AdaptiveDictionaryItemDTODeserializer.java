package org.shoulder.web.template.dictionary.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.core.util.ReflectionKit;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;

import java.io.IOException;

/**
 * 支持前端传 String，后端用 DictionaryItemDTO 接收
 *
 * @param <DICTIONARY_TYPE> 要转换的字典类型
 * @author lym
 */
public abstract class AdaptiveDictionaryItemDTODeserializer<DICTIONARY_TYPE> extends JsonDeserializer<DictionaryItemDTO> {

    private final DictionaryItemDTODeserializer delegate;

    public AdaptiveDictionaryItemDTODeserializer() {
        Class<? extends DictionaryItemEnum> clazz = (Class<? extends DictionaryItemEnum>) ReflectionKit.getSuperClassGenericType(getClass(), AdaptiveDictionaryItemDTODeserializer.class, 0);
        this.delegate = new DictionaryItemDTODeserializer(clazz);
    }

    @Override
    public DictionaryItemDTO deserialize(JsonParser p, DeserializationContext context) throws IOException {
        return delegate.deserialize(p, context);
    }

}
