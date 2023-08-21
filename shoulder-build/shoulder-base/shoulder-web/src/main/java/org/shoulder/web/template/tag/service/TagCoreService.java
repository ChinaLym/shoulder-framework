package org.shoulder.web.template.tag.service;

import org.apache.commons.lang3.StringUtils;
import org.shoulder.web.template.tag.entity.TagEntity;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 标签服务
 *
 * @author lym
 */
public interface TagCoreService {

    /**
     * 根据 type /（name like） 查询所有标签
     *
     * @param type          bizType
     * @param searchContent tagNameLike
     * @return tagList
     */
    List<TagEntity> searchTagByBizTypeAndName(String type, String searchContent);

    @Nullable
    TagEntity queryTagById(Long id);

    @Nullable
    List<TagEntity> queryTagByIdList(List<Long> idList);

    /**
     * 根据 bizType, name 查 tag
     *
     * @param bizType bizType
     * @param name    name
     * @return tag
     */
    @Nullable
    TagEntity queryTagByBizTypeAndName(String bizType, String name);

    /**
     * 根据 bizType, name 查 TagEntity
     *
     * @param bizType  bizType
     * @param nameList name
     * @return tagList
     */
    List<TagEntity> queryTagByBizTypeAndNameList(String bizType, List<String> nameList);

    /**
     * 根据 refType, tagId 搜索所有 refId
     *
     * @param refType 存储位置
     * @param tagId   tagId
     * @return refIdList
     */
    default Set<Long> queryAllRefIdByStorageSourceAndTagId(String refType, Long tagId) {
        return queryAllRefIdByStorageSourceAndTagIdList(refType, Collections.singletonList(tagId));
    }

    /**
     * 根据 refType, tagId 搜索所有 refId
     *
     * @param refType   存储位置
     * @param tagIdList tagIdList
     * @return refIdList
     */
    Set<Long> queryAllRefIdByStorageSourceAndTagIdList(String refType, List<Long> tagIdList);

    // ================

    default void attachTag(String refType, Long refId, TagEntity tag) {
        attachTags(refType, refId, Collections.singletonList(tag));
    }

    void attachTags(String refType, Long refId, List<TagEntity> tagList);

    /**
     * 给 refType 的 refId 打标签
     *
     * @param refType     需要添加标签的记录存储类型
     * @param refId       需要添加标签的记录的标识 long / String 都支持；目前仅开放 long 的使用，存主键 id
     * @param bizType     bizType
     * @param tagNameList 需要添加的标签(最好不要包含已有的)
     * @return tagList
     */
    List<TagEntity> attachTags(String refType, Long refId, String bizType, List<String> tagNameList);

    /**
     * 给 refType 的 refId 打标签
     *
     * @param refType 需要添加标签的记录存储类型
     * @param refId   需要添加标签的记录的标识 long / String
     * @param bizType bizType
     * @param tagName 需要添加的标签(最好不要包含已有的)
     * @return tag
     */
    @Nonnull
    default TagEntity attachTag(String refType, Long refId, String bizType, String tagName) {
        return attachTags(refType, refId, bizType, Collections.singletonList(tagName)).get(0);
    }

    /**
     * 删除标签关联关系
     *
     * @param refType   需要添加标签的记录存储类型
     * @param refId     需要添加标签的记录的标识 long / String
     * @param tagIdList 需要删除的标签
     */
    void removeTagSearches(String refType, Long refId, List<Long> tagIdList);

    /**
     * 更新标签关系
     *
     * @param refType 需要添加标签的记录存储类型
     * @param refId   需要添加标签的记录的标识 long / String
     */
    void updateTagSearches(String refType, String bizType, Long refId, String oldName, String newName);

    /**
     * 批量更新标签关系
     *
     * @param refType 需要添加标签的记录存储类型
     * @param refId   需要添加标签的记录的标识 long / String
     */
    void batchUpdateTagSearches(String refType, String bizType, Long refId, List<String> oldNames, List<String> newNames);

    /**
     * tagList1 - tagList2
     */
    default List<TagEntity> subtract(List<TagEntity> tagList1, List<TagEntity> tagList2) {
        if (CollectionUtils.isEmpty(tagList1)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(tagList2)) {
            return tagList1;
        }
        List<TagEntity> subList = new ArrayList<>(tagList1.size());
        for (TagEntity tag : tagList1) {
            boolean existInTagList2 = false;
            for (TagEntity tag2 : tagList2) {
                if (StringUtils.equals(tag.getBizType(), tag2.getBizType()) && StringUtils.equals(tag.getName(), tag2.getName())) {
                    existInTagList2 = true;
                }
            }
            if (!existInTagList2) {
                subList.add(tag);
            }
        }
        return subList;
    }

    void ensureExistOrCreateTagList(List<TagEntity> tagList);
}
