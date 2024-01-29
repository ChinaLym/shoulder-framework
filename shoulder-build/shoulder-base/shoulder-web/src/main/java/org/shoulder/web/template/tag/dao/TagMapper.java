package org.shoulder.web.template.tag.dao;

import org.apache.ibatis.annotations.Mapper;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.web.template.tag.entity.TagEntity;

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


}
