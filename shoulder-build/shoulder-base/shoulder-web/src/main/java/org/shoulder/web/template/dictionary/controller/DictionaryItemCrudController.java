package org.shoulder.web.template.dictionary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.web.template.crud.CrudCacheableController;
import org.shoulder.web.template.dictionary.dto.DictionaryItemDTO;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.shoulder.web.template.dictionary.service.DictionaryItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 枚举型字典接口-默认实现
 *
 * @author lym
 */
@Tag(name = "DictionaryItemCrudController", description = "字典-明细项管理")
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.apiPath + '/item':/api/v1/dictionary/item}")
public class DictionaryItemCrudController extends CrudCacheableController<
        DictionaryItemService,
        DictionaryItemEntity,
        Long,
    DictionaryItemDTO,
    DictionaryItemDTO,
    DictionaryItemDTO,
    DictionaryItemDTO
    > implements DictionaryItemController {

    public DictionaryItemCrudController(DictionaryItemService service, ShoulderConversionService conversionService) {
        super(service, conversionService);
    }

}
