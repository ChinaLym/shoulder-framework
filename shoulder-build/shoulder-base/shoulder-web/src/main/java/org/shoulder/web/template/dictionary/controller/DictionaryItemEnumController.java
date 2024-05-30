package org.shoulder.web.template.dictionary.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.core.dictionary.spi.DictionaryEnumStore;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.web.template.dictionary.dto.DictionaryBatchQueryParam;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.dto.DictionaryTypeDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@Tag(name = "DictionaryItemEnumController", description = "枚举字典-枚举项查询(只读)")
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.path:/api/v1/dictionary}")
public class DictionaryItemEnumController implements DictionaryItemController {

    /**
     * 字典枚举存储
     */
    private final DictionaryEnumStore dictionaryEnumStore;

    protected final ShoulderConversionService conversionService;

    public DictionaryItemEnumController(DictionaryEnumStore dictionaryEnumStore, ShoulderConversionService conversionService) {
        this.dictionaryEnumStore = dictionaryEnumStore;
        this.conversionService = conversionService;
    }

    /**
     * 查询单个字典的所有字典项
     *
     * @param dictionaryType 字典项类型
     * @return 查询结果
     */
    @Parameters({
            @Parameter(name = "dictionaryType", description = "字典类型"),
    })
    @Operation(summary = "查询单个字典项", description = "查询单个字典项")
    @GetMapping("/listByType/{dictionaryType}")
    public BaseResult<ListResult<DictionaryItemDTO>> listByType(@OperationLogParam @PathVariable("dictionaryType") String dictionaryType) {
        List<DictionaryItemDTO> dictionaryItemList = query(dictionaryType);
        //dictionaryItemList.sort(DictionaryItemDTO::compareTo); // 枚举添加的时候已经排序了，暂不需要这行代码
        return BaseResult.success(dictionaryItemList);
    }

    /**
     * 根据类型查询多个字典项
     *
     * @param batchQueryParam 字典项类型 List
     * @return 查询结果
     */
    @Parameters({
            @Parameter(name = "batchQueryParam", description = "字典类型 list"),
    })
    @Operation(summary = "查询多个字典项", description = "查询多个字典项")
    @RequestMapping(value = "/listByTypes", method = { RequestMethod.GET, RequestMethod.POST })
    public BaseResult<ListResult<DictionaryTypeDTO>> listAllByTypes(@Validated DictionaryBatchQueryParam batchQueryParam) {
        List<DictionaryTypeDTO> dictionaryList = batchQueryParam.getDictionaryTypeList().stream()
            .map(type -> new DictionaryTypeDTO(type, query(type)))
            .collect(Collectors.toList());
        return BaseResult.success(dictionaryList);
    }

    @SuppressWarnings("rawtypes")
    private List<DictionaryItemDTO> query(String dictionaryType) {
        List<Enum<? extends DictionaryItemEnum>> enumItems = dictionaryEnumStore.listAllAsDictionaryEnum(dictionaryType);
        return enumItems.stream()
                .map(e -> (DictionaryItemEnum) e)
                .map(d -> conversionService.convert(d, DictionaryItemDTO.class))
            .collect(Collectors.toList());
    }

}
