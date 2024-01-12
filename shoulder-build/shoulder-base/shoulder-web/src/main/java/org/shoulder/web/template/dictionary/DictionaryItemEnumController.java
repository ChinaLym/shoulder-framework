package org.shoulder.web.template.dictionary;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.web.template.dictionary.dto.DictionaryBatchQueryParam;
import org.shoulder.web.template.dictionary.dto.DictionaryDTO;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 枚举型字典接口-默认实现
 * todo暂时不支持search，过滤由前端缓存后过滤，也避免每次咨询后端api
 * http://localhost:8080/api/v1/dictionary/item/listByType/xxx
 * http://localhost:8080/api/v1/dictionary/item/listByTypes?xxx
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
    public BaseResult<ListResult<DictionaryItemDTO>> listByType(@OperationLogParam @PathVariable("dictionaryType") String dictionaryType) {
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
    @RequestMapping(value = "/listByTypes", method = { RequestMethod.GET, RequestMethod.POST })
    public BaseResult<ListResult<DictionaryDTO>> listAllByTypes(@Validated DictionaryBatchQueryParam batchQueryParam) {
        List<DictionaryDTO> dictionaryList = batchQueryParam.getDictionaryTypeList().stream()
            .map(type -> new DictionaryDTO(type, query(type)))
            .collect(Collectors.toList());
        return BaseResult.success(dictionaryList);
    }

    @SuppressWarnings("rawtypes")
    private List<DictionaryItemDTO> query(String dictionaryType) {
        List<Enum<? extends DictionaryEnum>> enumItems = dictionaryEnumStore.listAllAsDictionaryEnum(dictionaryType);
        return enumItems.stream()
            .map(e -> (DictionaryEnum) e)
            .map(d -> new DictionaryItemDTO(d.getName(), d.getDisplayName()))
            .collect(Collectors.toList());
    }

}
