package org.shoulder.web.template.dictionary;

import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.web.template.dictionary.dto.BatchQueryParam;
import org.shoulder.web.template.dictionary.dto.DictionaryDTO;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举型字典接口-默认实现
 *
 * @author lym
 */
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.path:/api/v1/dictionary}")
public class DefaultDictionaryController implements DictionaryController {

    /**
     * 字典枚举存储
     */
    private final DictionaryEnumRepository dictionaryEnumRepository;

    public DefaultDictionaryController(DictionaryEnumRepository dictionaryEnumRepository) {
        this.dictionaryEnumRepository = dictionaryEnumRepository;
    }


    @Override
    public BaseResult<ListResult<String>> allTypes() {
        Collection<String> allTypeNames = dictionaryEnumRepository.listAllTypeNames();
        return BaseResult.success(allTypeNames);
    }

    @Override
    public BaseResult<ListResult<DictionaryItemDTO>> list(String dictionaryType) {
        List<DictionaryItemDTO> dictionaryItemList = query(dictionaryType);
        return BaseResult.success(dictionaryItemList);
    }

    @Override
    public BaseResult<ListResult<DictionaryDTO>> batchList(BatchQueryParam batchQueryParam) {
        List<DictionaryDTO> dictionaryList = batchQueryParam.getDictionaryTypeList().stream()
                .map(type -> new DictionaryDTO(type, query(type)))
                .collect(Collectors.toList());
        return BaseResult.success(dictionaryList);
    }

    @SuppressWarnings("rawtypes")
    private List<DictionaryItemDTO> query(String dictionaryType) {
        List<DictionaryEnum> enumItems = dictionaryEnumRepository.listAsDictionaryEnum(dictionaryType);
        return enumItems.stream()
                .map(d -> new DictionaryItemDTO(d.getName(), d.getDisplayName()))
                .collect(Collectors.toList());
    }

}
