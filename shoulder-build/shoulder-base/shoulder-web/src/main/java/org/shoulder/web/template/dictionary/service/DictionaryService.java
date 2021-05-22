package org.shoulder.web.template.dictionary.service;

import org.shoulder.data.mybatis.template.service.BaseCacheableServiceImpl;
import org.shoulder.web.template.dictionary.model.DictionaryEntity;
import org.shoulder.web.template.dictionary.repository.DictionaryRepository;

import java.io.Serializable;

/**
 * @author lym
 */
public class DictionaryService<ID extends Serializable> extends BaseCacheableServiceImpl<DictionaryRepository<ID>, DictionaryEntity<ID>> {

}
