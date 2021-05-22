package org.shoulder.web.template.tag.dao;

import org.apache.ibatis.annotations.Mapper;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.web.template.tag.entity.TagRelationShipEntity;

/**
 * 字典类型存储
 * <p>
 * 不要使用 @Mapper / @Repository
 *
 * @author lym
 */
@Mapper
public interface TagRelationShipMapper extends BaseMapper<TagRelationShipEntity> {

}
