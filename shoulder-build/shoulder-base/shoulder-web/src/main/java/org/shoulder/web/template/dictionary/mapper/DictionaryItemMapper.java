package org.shoulder.web.template.dictionary.mapper;

import org.apache.ibatis.annotations.Mapper;
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
@Mapper
public interface DictionaryItemMapper<ID extends Serializable> extends BaseMapper<DictionaryItemEntity<ID>> {

}
