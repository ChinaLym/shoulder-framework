package org.shoulder.web.template.tag.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.web.template.tag.entity.TagMappingEntity;

/**
 * 字典类型存储
 * <p>
 * 不要使用 @Mapper / @Repository
 *
 * @author lym
 */
@Mapper
public interface TagMappingMapper extends BaseMapper<TagMappingEntity> {

    @Update("SELECT * FROM tb_tag_mapping WHERE tag_id = #{tagId} and biz_type = #{bizType} and ref_id = #{refId} FOR UPDATE")
    TagMappingEntity selectForUpdate(Long tagId, String bizType, String refId);
}
