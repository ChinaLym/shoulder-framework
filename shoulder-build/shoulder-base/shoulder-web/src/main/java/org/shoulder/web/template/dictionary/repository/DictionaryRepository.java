package org.shoulder.web.template.dictionary.repository;

import org.shoulder.data.mybatis.template.dao.FakerMapper;
import org.shoulder.web.template.dictionary.model.DictionaryEntity;

import java.io.Serializable;

/**
 * 字典类型存储
 * <p>
 * 不要使用 @Mapper / @Repository
 *
 * @author lym
 */
public interface DictionaryRepository<ID extends Serializable> extends FakerMapper<DictionaryEntity<ID>> {

}
