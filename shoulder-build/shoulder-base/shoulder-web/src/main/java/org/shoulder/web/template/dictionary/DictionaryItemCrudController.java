package org.shoulder.web.template.dictionary;

import org.shoulder.web.template.crud.CrudCacheableController;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.shoulder.web.template.dictionary.service.DictionaryItemService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

/**
 * 枚举型字典接口-默认实现
 *
 * @author lym
 */
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.path + '/item':/api/v1/dictionary/item}")
public class DictionaryItemCrudController<ID extends Serializable> extends CrudCacheableController<
        DictionaryItemService<ID>, DictionaryItemEntity<ID>, ID, DictionaryItemEntity<ID>, DictionaryItemEntity<ID>, DictionaryItemEntity<ID>
        > implements DictionaryItemController {

}
