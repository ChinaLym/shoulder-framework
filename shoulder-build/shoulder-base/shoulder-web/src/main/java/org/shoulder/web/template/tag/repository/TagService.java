package org.shoulder.web.template.tag.repository;

import org.shoulder.data.mybatis.template.service.BaseCacheableServiceImpl;
import org.shoulder.web.template.tag.dao.TagMapper;
import org.shoulder.web.template.tag.entity.TagEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TagService
 *
 * @author lym
 * @deprecated 暂未实现完善，无法使用
 */
public class TagService extends BaseCacheableServiceImpl<TagMapper, TagEntity> {

    @Autowired(required = false)
    private TagSearchShipService tagSearchShipService;

    @Autowired(required = false)
    private TagRelationShipService relationShipService;


    public boolean createTags(List<TagEntity> tagEntityList) {
        // max 100
        assert tagEntityList.size() < 100;

        // 尝试 select for update 这些
        List<String> bizIdList = tagEntityList.stream()
                .map(TagEntity::getBizId)
                .collect(Collectors.toList());
        // 数据库中已经存在的
        List<TagEntity> existsTagEntities = getBaseMapper().selectBatchForUpdateByBizIds(bizIdList);
        Set<String> existsBizIds = existsTagEntities.stream()
                .map(TagEntity::getBizId)
                .collect(Collectors.toSet());
        // 过滤出没有的
        List<TagEntity> toSaveEntityList = tagEntityList.stream()
                .filter(e -> !existsBizIds.contains(e.getBizId()))
                .collect(Collectors.toList());

        // 批量插入
        return getBaseMapper().insertBatch(toSaveEntityList) == toSaveEntityList.size();
    }

    boolean attachTags(String bizType, String refId, List<Long> tagIds) {
        // max 20
        assert tagIds.size() < 20;
        // select for update 这些 tagIds 对应的标签关系
        // 过滤出没有的
        // 创建标签
        // 创建标签关联关系
        for (Long tagId : tagIds) {
            attachTag(bizType, refId, tagId);
        }
        return true;
    }
    // max 100 batch｜ insert

    boolean attachTag(String bizType, String refId, Long tagId) {
        // select for update tagId 对应的标签关系

        // 有则返回，没有则创建标签
        return false;
    }


    List<String> searchByTagId(Long id) {
        // 查标签id，获取外部业务表 bizId
        return null;
    }

    //List<String> searchByAllTagIds(List<Long> ids);

    //List<String> searchByAnyTagIds(List<Long> ids);

    List<String> searchByTagIds(List<List<Long>> ids) {
        // 查标签id，获取外部业务表 bizId
        return null;
    }

}
