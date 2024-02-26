package org.shoulder.web.template.tag.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.web.template.crud.CrudCacheableController;
import org.shoulder.web.template.tag.dto.BaseSearchRequest;
import org.shoulder.web.template.tag.dto.TagDTO;
import org.shoulder.web.template.tag.model.TagEntity;
import org.shoulder.web.template.tag.service.TagCoreService;
import org.shoulder.web.template.tag.service.TagServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tag 接口-默认实现
 * 由于 Tag 可以不作为暴露接口，故默认不注入
 *
 * @author lym
 */
@RestController
@RequestMapping(value = "${shoulder.web.ext.tag.path:/api/v1/tags}")
public class TagCrudController extends CrudCacheableController<
    TagServiceImpl,
    TagEntity,
    Long,
    TagDTO,
    TagDTO,
    TagDTO,
    TagDTO>
    implements TagController {

    @Autowired
    private TagCoreService tagCoreService;

    //save检查标签操作权限

    /**
     * 常用于前端根据用户输入信息进行提示
     */
    @Validated
    @RequestMapping(method = RequestMethod.GET, value = "search.json")
    @ResponseBody
    public BaseResult<ListResult<TagDTO>> search(@Valid @NotNull BaseSearchRequest request) {
        List<TagEntity> searchResult = tagCoreService.searchTagByBizTypeAndName(
            request.getBizType(), request.getSearchContent(), request.getLimit());

        List<TagDTO> list = conversionService.convert(searchResult, TagDTO.class);
        return BaseResult.success(list);
    }

}
