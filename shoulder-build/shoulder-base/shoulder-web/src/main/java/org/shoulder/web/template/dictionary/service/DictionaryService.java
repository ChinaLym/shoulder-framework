package org.shoulder.web.template.dictionary.service;

import org.shoulder.data.mybatis.template.service.BaseCacheableServiceImpl;
import org.shoulder.web.template.dictionary.mapper.DictionaryMapper;
import org.shoulder.web.template.dictionary.model.DictionaryTypeEntity;

import java.io.Serializable;

/**
 * @author lym
 */
public class DictionaryService<ID extends Serializable> extends BaseCacheableServiceImpl<DictionaryMapper<ID>, DictionaryTypeEntity<ID>> {

}
