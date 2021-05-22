package org.shoulder.web.template.dictionary.service;

import org.shoulder.data.mybatis.template.service.BaseCacheableServiceImpl;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.shoulder.web.template.dictionary.repository.DictionaryItemRepository;

import java.io.Serializable;

/**
 * itemService
 *
 * @author lym
 */
public class DictionaryItemService<ID extends Serializable> extends BaseCacheableServiceImpl<DictionaryItemRepository<ID>, DictionaryItemEntity<ID>> {

}
