package org.shoulder.web.template.dictionary.service;

import org.shoulder.core.dictionary.model.DictionaryItem;
import org.shoulder.data.mybatis.template.service.BaseCacheableServiceImpl;
import org.shoulder.web.template.dictionary.convert.String2ConfigAbleDictionaryItemConverter;
import org.shoulder.web.template.dictionary.mapper.DictionaryItemMapper;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * itemService
 *
 * @author lym
 */
public class DictionaryItemService
        extends BaseCacheableServiceImpl<DictionaryItemMapper, DictionaryItemEntity>
        implements String2ConfigAbleDictionaryItemConverter {

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
        return result;
    }

    public DictionaryItemEntity getByTypeAndCodeFromCache(String dictionaryType, String itemCode) {
        String cacheKey = generateCacheKey("dicType_" + dictionaryType);
        Map<String, DictionaryItemEntity> dictMap = cache.get(cacheKey,
            () -> buildDictionaryTypeItemCacheMap(dictionaryType));
        return dictMap.get(itemCode);
    }

    private Map<String, DictionaryItemEntity> buildDictionaryTypeItemCacheMap(String dictionaryType) {
        Map<String, DictionaryItemEntity> itemMap = new ConcurrentHashMap<>();
        List<DictionaryItemEntity> itemList = listItemByDictionaryType(dictionaryType);
        if(!CollectionUtils.isEmpty(itemList)) {
            for (DictionaryItemEntity item : itemList) {
                itemMap.put(item.getItemId(), item);
            }
        }
        return itemMap;
    }

    public List<DictionaryItemEntity> listItemByDictionaryType(String dictionaryType) {
        return super.getBaseMapper().selectList(super.lambdaQuery()
            // 查出所有，未限制个数
            .eq(DictionaryItem::getDictionaryType, dictionaryType));
    }

    protected void evictCache(DictionaryItemEntity model) {
        super.evictCache(model);
        if(model.getDictionaryType() != null) {
            String cacheKey = generateCacheKey("dicType_" + model.getDictionaryType());
            cache.evict(cacheKey);
        }
    }
    protected void buildCache(DictionaryItemEntity model) {
        super.buildCache(model);
        if(model.getDictionaryType() != null) {
            String cacheKey = generateCacheKey("dicType_" + model.getDictionaryType());
            Map<String, DictionaryItemEntity> itemMap = buildDictionaryTypeItemCacheMap(model.getDictionaryType());
            cache.put(cacheKey, itemMap);
        }
    }
}
