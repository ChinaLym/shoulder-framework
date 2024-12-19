package org.shoulder.web.template.dictionary.convert;

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
 * @author lym
 */
public abstract class DictionaryItemDTODeserializer extends JsonDeserializer<DictionaryItemDTO> {

    /**
     * 实际枚举字典类型
     */
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
//        if (!text.startsWith("{") && text.endsWith("}")) {
        DictionaryItemEnum dictionaryItemEnum = ConvertUtil.convert(text, dictionaryClass);
        return ConvertUtil.convert(dictionaryItemEnum, DictionaryItemDTO.class);
//        }
        // todo 直接转化
//        return null;

    }
}
