package org.shoulder.core.dictionary.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.core.util.ConvertUtil;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;

import java.io.IOException;

/**
 * 支持前端传 String，后端用 DictionaryItemDTO 接收
 *
 * @param <DICTIONARY_TYPE> 字典枚举类型
 * @author lym
 */
public abstract class DictionaryItemDTODeserializer extends JsonDeserializer<DictionaryItemDTO> {

    protected final Class<? extends DictionaryItemEnum> dictionaryClass;

    public DictionaryItemDTODeserializer(Class<? extends DictionaryItemEnum> dictionaryClass) {
        this.dictionaryClass = dictionaryClass;
    }

    @Override
    public DictionaryItemDTO deserialize(JsonParser p, DeserializationContext context) throws IOException {
        String text = p.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        DictionaryItemEnum dictionaryItemEnum = ConvertUtil.convert(text, dictionaryClass);
        return ConvertUtil.convert(dictionaryItemEnum, DictionaryItemDTO.class);
    }
}
