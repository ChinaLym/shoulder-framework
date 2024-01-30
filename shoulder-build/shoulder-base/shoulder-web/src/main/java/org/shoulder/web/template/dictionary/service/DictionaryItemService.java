package org.shoulder.web.template.dictionary.service;

import org.shoulder.data.mybatis.template.service.BaseCacheableServiceImpl;
import org.shoulder.web.template.dictionary.mapper.DictionaryItemMapper;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.shoulder.web.template.dictionary.spi.String2ConfigAbleDictionaryItemConverter;

import java.io.Serializable;

/**
 * itemService
 *
 * @author lym
 */
public class DictionaryItemService<ID extends Serializable> extends BaseCacheableServiceImpl<DictionaryItemMapper<ID>, DictionaryItemEntity<ID>> implements String2ConfigAbleDictionaryItemConverter {

    @Override
    public ConfigAbleDictionaryItem convertToConfigAbleDictionaryItem(String dictionaryType, String code) {
        if (code == null) {
            return null;
        }
        // todo 查bizId and name
        ConfigAbleDictionaryItem result = new ConfigAbleDictionaryItem();
        result.setDictionaryType(dictionaryType);
        result.setCode(code);
        result.setDisplayName(code);
        result.setDisplayOrder(0);
        return result;

    }
}
