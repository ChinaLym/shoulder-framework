package org.shoulder.web.template.dictionary.repository;

import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;

import java.io.Serializable;

/**
 * 字典类型存储
 * <p>
 * 不要使用 @Mapper / @Repository
 *
 * @author lym
 */
public interface DictionaryItemRepository<ID extends Serializable> extends BaseMapper<DictionaryItemEntity<ID>> {

}
