package org.shoulder.web.template.dictionary;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.web.template.dictionary.dto.BatchQueryParam;
import org.shoulder.web.template.dictionary.dto.DictionaryDTO;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 字典项接口
 *
 * @author lym
 */
public interface DictionaryItemController {

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
    BaseResult<ListResult<DictionaryItemDTO>> listByType(@PathVariable(value = "dictionaryType") String dictionaryType);


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
    BaseResult<ListResult<DictionaryDTO>> listAllByTypes(@Valid @NotNull BatchQueryParam batchQueryParam);
}
