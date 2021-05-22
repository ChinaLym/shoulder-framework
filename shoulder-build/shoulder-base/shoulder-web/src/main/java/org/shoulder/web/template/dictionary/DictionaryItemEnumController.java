package org.shoulder.web.template.dictionary;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.web.template.dictionary.dto.BatchQueryParam;
import org.shoulder.web.template.dictionary.dto.DictionaryDTO;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举型字典接口-默认实现
 *
 * @author lym
 */
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.path + '/item':/api/v1/dictionary/item}")
public class DictionaryItemEnumController implements DictionaryItemController {

    /**
     * 字典枚举存储
     */
    private final DictionaryEnumStore dictionaryEnumStore;

    public DictionaryItemEnumController(DictionaryEnumStore dictionaryEnumStore) {
        this.dictionaryEnumStore = dictionaryEnumStore;
    }

    /**
     * 查询单个字典的所有字典项
     *
     * @param dictionaryType 字典项类型
     * @return 查询结果
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dictionaryType", value = "字典类型", dataType = "String", paramType = "path"),
    })
    @ApiOperation(value = "查询单个字典项", notes = "查询单个字典项")
    @GetMapping("/listByType/{dictionaryType}")
    public BaseResult<ListResult<DictionaryItemDTO>> listByType(String dictionaryType) {
        List<DictionaryItemDTO> dictionaryItemList = query(dictionaryType);
        return BaseResult.success(dictionaryItemList);
    }

    /**
     * 根据类型查询多个字典项
     *
     * @param batchQueryParam 字典项类型 List
     * @return 查询结果
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = "batchQueryParam", value = "字典类型 list", dataType = "List", paramType = "body"),
    })
    @ApiOperation(value = "查询多个字典项", notes = "查询多个字典项")
    @PostMapping("/listByTypes")
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
