package org.shoulder.web.template.dictionary.repository;

import org.shoulder.data.mybatis.template.dao.FakerMapper;
import org.shoulder.web.template.dictionary.model.DictionaryItemEntity;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;

import java.io.Serializable;

/**
 * 字典类型存储 - 枚举型
 * 仅支持查；不支持增删改，若增删，通过 {@link DictionaryEnumStore}
 *
 * @author lym
 */
public class DictionaryRepositoryEnumImpl<ID extends Serializable> implements DictionaryItemRepository<ID>, FakerMapper<DictionaryItemEntity<ID>> {

}
