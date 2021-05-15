package org.shoulder.web.template.dictionary.repository;

import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;

/**
 * 字典类型存储
 * <p>
 * 不要使用 @Mapper / @Repository
 *
 * @author lym
 */
@Mapper
public interface DictionaryRepositoryDbImpl<ID extends Serializable> extends DictionaryRepository<ID> {

}
