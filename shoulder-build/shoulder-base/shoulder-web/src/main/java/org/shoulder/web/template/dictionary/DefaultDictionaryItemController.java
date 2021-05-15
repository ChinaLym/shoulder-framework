package org.shoulder.web.template.dictionary;

import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.web.template.crud.CrudCacheableController;
import org.shoulder.web.template.dictionary.dto.BatchQueryParam;
import org.shoulder.web.template.dictionary.dto.DictionaryDTO;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.shoulder.web.template.dictionary.service.DictionaryItemService;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举型字典接口-默认实现
 *
 * @author lym
 */
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.path:/api/v1/dictionary}")
public class DefaultDictionaryItemController<ID extends Serializable> extends CrudCacheableController<
        DictionaryItemService<ID>, DictionaryItemEntity<ID>, ID, DictionaryItemEntity<ID>, DictionaryItemEntity<ID>, DictionaryItemEntity<ID>
        > implements DictionaryItemController {

    /**
     * 字典枚举存储
     */
    private final DictionaryEnumStore dictionaryEnumStore;

    public DefaultDictionaryItemController(@Autowired(required = false) DictionaryEnumStore dictionaryEnumStore) {
        this.dictionaryEnumStore = dictionaryEnumStore;
    }

    @Override
    public BaseResult<ListResult<DictionaryItemDTO>> listByType(String dictionaryType) {
        List<DictionaryItemDTO> dictionaryItemList = query(dictionaryType);
        return BaseResult.success(dictionaryItemList);
    }

    @Override
    public BaseResult<ListResult<DictionaryDTO>> listAllByTypes(BatchQueryParam batchQueryParam) {
        List<DictionaryDTO> dictionaryList = batchQueryParam.getDictionaryTypeList().stream()
                .map(type -> new DictionaryDTO(type, query(type)))
                .collect(Collectors.toList());
        return BaseResult.success(dictionaryList);
    }

    @SuppressWarnings("rawtypes")
    private List<DictionaryItemDTO> query(String dictionaryType) {
        List<DictionaryEnum> enumItems = dictionaryEnumStore.listAllAsDictionaryEnum(dictionaryType);
        return enumItems.stream()
                .map(d -> new DictionaryItemDTO(d.getName(), d.getDisplayName()))
                .collect(Collectors.toList());
    }

}
