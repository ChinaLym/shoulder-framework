package org.shoulder.web.template.tag.controller;

import org.shoulder.web.template.crud.CrudCacheableController;
import org.shoulder.web.template.tag.entity.TagEntity;
import org.shoulder.web.template.tag.service.TagServiceImpl;

/**
 * Tag 接口-默认实现
 * 由于 Tag 可以不作为暴露接口，故默认不注入
 *
 * @author lym
 */
//@RestController
//@RequestMapping(value = "${shoulder.web.ext.tag.path:/api/v1/tags}")
public class TagCrudController extends CrudCacheableController<
        TagServiceImpl, TagEntity, Long, TagEntity, TagEntity, TagEntity
        > implements TagController {


}
