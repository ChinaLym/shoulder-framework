package org.shoulder.web.template.dictionary;

import org.shoulder.web.template.crud.CrudCacheableController;
import org.shoulder.web.template.dictionary.model.DictionaryEntity;
import org.shoulder.web.template.dictionary.service.DictionaryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

/**
 * 枚举型字典接口-默认实现
 *
 * @author lym
 */
@RestController
@RequestMapping(value = "${shoulder.web.ext.dictionary.path:/api/v1/dictionary}")
public class DictionaryCrudController<ID extends Serializable> extends CrudCacheableController<
        DictionaryService<ID>, DictionaryEntity<ID>, ID, DictionaryEntity<ID>, DictionaryEntity<ID>, DictionaryEntity<ID>
        > implements DictionaryController {

}
