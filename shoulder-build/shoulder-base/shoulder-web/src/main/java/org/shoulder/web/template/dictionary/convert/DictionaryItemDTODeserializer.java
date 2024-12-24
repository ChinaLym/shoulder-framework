package org.shoulder.web.template.dictionary.convert;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.ConvertUtil;
import org.shoulder.validate.exception.ParamErrorCodeEnum;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;

import java.io.IOException;
import java.util.Map;

/**
 * 支持前端传 String，后端用 DictionaryItemDTO 接收
 *
 * @author lym
 */
public class DictionaryItemDTODeserializer extends JsonDeserializer<DictionaryItemDTO> {

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

        if (text.startsWith("{")) {
            Object parseResult = new UntypedObjectDeserializer().deserialize(p, context);
            AssertUtils.isTrue(parseResult != null && parseResult instanceof Map, ParamErrorCodeEnum.PARAM_ILLEGAL, context.getParser().getParsingContext().getCurrentName());
            Map parsedMap = (Map) parseResult;
            AssertUtils.notEmpty(parsedMap, ParamErrorCodeEnum.PARAM_ILLEGAL, context.getParser().getParsingContext().getCurrentName());
            String dictionaryCode = (String) parsedMap.get("code");
            AssertUtils.notBlank(dictionaryCode, ParamErrorCodeEnum.PARAM_ILLEGAL, context.getParser().getParsingContext().getCurrentName());

            DictionaryItemEnum dictionaryItemEnum = ConvertUtil.convert(dictionaryCode, dictionaryClass);
            return ConvertUtil.convert(dictionaryItemEnum, DictionaryItemDTO.class);
        }

        DictionaryItemEnum dictionaryItemEnum = ConvertUtil.convert(text, dictionaryClass);
        return ConvertUtil.convert(dictionaryItemEnum, DictionaryItemDTO.class);

    }
}
