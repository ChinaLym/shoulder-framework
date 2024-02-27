package org.shoulder.web.template.tag.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.web.template.tag.model.TagMappingEntity;

/**
 * 字典类型存储
 * <p>
 * 不要使用 @Mapper / @Repository
 *
 * @author lym
 */
@Mapper
public interface TagMappingMapper extends BaseMapper<TagMappingEntity> {

    @Select("SELECT * FROM tb_tag_mapping WHERE tag_id = #{tagId} and ref_type = #{refType} and oid = #{refId} FOR UPDATE")
    TagMappingEntity selectForUpdate(Long tagId, String refType, String refId);
}
