package org.shoulder.web.template.dictionary.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.shoulder.core.converter.ShoulderConversionService;
import org.shoulder.web.template.crud.CrudCacheableController;
import org.shoulder.web.template.dictionary.dto.DictionaryTypeDTO;
import org.shoulder.web.template.dictionary.model.DictionaryTypeEntity;
import org.shoulder.web.template.dictionary.service.DictionaryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 枚举型字典接口-默认实现
 *
 * @author lym
 */
@Tag(name = "DictionaryTypeCrudController", description = "字典-类型管理")
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.path + '/type':/api/v1/dictionary/type}")
public class DictionaryTypeCrudController
    extends CrudCacheableController<
        DictionaryService,
        DictionaryTypeEntity,
        Long,
    DictionaryTypeDTO,
    DictionaryTypeDTO,
    DictionaryTypeDTO,
    DictionaryTypeDTO>
        implements DictionaryTypeController {

    public DictionaryTypeCrudController(DictionaryService service, ShoulderConversionService conversionService) {
        super(service, conversionService);
    }
}
