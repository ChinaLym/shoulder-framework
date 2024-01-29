package org.shoulder.web.template.tag.controller;

import org.shoulder.web.template.crud.CrudCacheableController;
import org.shoulder.web.template.tag.dto.TagDTO;
import org.shoulder.web.template.tag.entity.TagEntity;
import org.shoulder.web.template.tag.service.TagServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    TagDTO> implements TagController {

    //@Validated
    //@RequestMapping(method = RequestMethod.GET, value = "search.json")
    //@ResponseBody
    //public CommonResult<List<TagVO>> search(@Valid @NotNull TagSearchRequest request) {
    //    BizType bizType = BizType.findByName(request.getBizType());
    //    AssertUtil.notNull(bizType, IexpmngResultEnum.PARAM_ILLEGAL, "no such bizType: " + bizType);
    //    List<TagVO> list = tagBizService.search(bizType, request.getSearchContent());
    //    return CommonResult.success(list);
    //}

}
