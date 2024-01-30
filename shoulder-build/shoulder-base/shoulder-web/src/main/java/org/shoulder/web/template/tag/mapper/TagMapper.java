package org.shoulder.web.template.tag.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.web.template.tag.model.TagEntity;

import java.util.List;

/**
 * 标签数据访问层
 *
 * @author lym
 */
@Mapper
public interface TagMapper extends BaseMapper<TagEntity> {

    List<String> searchByTagId(Long id);

    List<String> searchByAllTagIds(List<Long> ids);

    List<String> searchByAnyTagIds(List<Long> ids);

    List<String> searchByTagIds(List<List<Long>> ids);

    @Update("SELECT * FROM tb_tag WHERE biz_type = #{bizType} and name IN #{tagNameList} FOR UPDATE")
    List<TagEntity> lockByTypeAndNameList(String bizType, List<String> tagNameList);
}
