package org.shoulder.web.template.tag.service;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.shoulder.web.template.tag.model.TagEntity;
import org.shoulder.web.template.tag.model.TagMappingEntity;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 标签服务
 *
 * - 根据name模糊查询：用户输入提示
 * - 基础 CRUD、根据id、idList、精确查询/锁定：标签管理
 * - 根据标签id、type 搜索（分页查询，按照 tag.order排序）
 *      - 根据标签idList、type 搜索（分页查询，按照 tag.order排序）
 * - 更新标签关系：
 *      - type+(name、nameList)  查询已有标签、锁定标签、标签关系；确保标签存在
 *      - 删除多余标签关系
 * - count 统计标签情况：分析标签分布
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
    List<TagEntity> searchTagByBizTypeAndName(String type, String searchContent, Integer limit);

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
    List<TagEntity> queryAllTagByBizTypeAndNameList(String bizType, List<String> nameList);

    /**
     * 根据 refType, tagId 搜索所有 oid
     *
     * @param refType 存储位置
     * @param tagId   tagId
     * @return refIdList
     */
    default List<TagMappingEntity> queryAllRefIdByStorageSourceAndTagId(String refType, Long tagId) {
        return queryAllRefIdByStorageSourceAndTagIdList(refType, Collections.singletonList(tagId));
    }

    /**
     * 根据 refType, tagId 搜索所有 oid
     *
     * @param refType   存储位置
     * @param tagIdList tagIdList
     * @return refIdList
     */
    List<TagMappingEntity> queryAllRefIdByStorageSourceAndTagIdList(String refType, List<Long> tagIdList);

    // ================

    default boolean attachTag(String refType, String oid, Long tag) {
        return attachTags(refType, oid, Collections.singletonList(tag));
    }

    boolean attachTags(String refType, String oid, List<Long> tagList);

    /**
     * 给 refType 的 oid 打标签；允许自动创建标签场景
     *
     * @param refType     需要添加标签的记录存储类型
     * @param oid       需要添加标签的记录的标识 long / String 都支持；目前仅开放 long 的使用，存主键 id
     * @param bizType     bizType
     * @param tagNameList 需要添加的标签(最好不要包含已有的)
     * @return tagList
     */
    List<TagEntity> attachTags(String refType, String oid, String bizType, List<String> tagNameList);

    /**
     * 给 refType 的 oid 打标签
     *
     * @param refType 需要添加标签的记录存储类型
     * @param oid   需要添加标签的记录的标识 long / String
     * @param type type
     * @param tagName 需要添加的标签(最好不要包含已有的)
     * @return tag
     */
    @Nonnull
    default TagEntity attachTag(String refType, String oid, String type, String tagName) {
        return attachTags(refType, oid, type, Collections.singletonList(tagName)).get(0);
    }

    /**
     * 删除标签关联关系
     *
     * @param refType   需要添加标签的记录存储类型
     * @param oid     需要添加标签的记录的标识 long / String
     * @param tagIdList 需要删除的标签
     */
    void removeTagMappings(String refType, String oid, List<Long> tagIdList);

    /**
     * 更新标签关系
     *
     * @param refType 需要添加标签的记录存储类型
     * @param oid   需要添加标签的记录的标识 long / String
     */
    void updateTagMappings(String refType, String bizType, String oid, String oldName, String newName);

    /**
     * 批量更新标签关系
     *
     * @param refType 需要添加标签的记录存储类型
     * @param oid   需要添加标签的记录的标识 long / String
     */
    void batchUpdateTagMappings(String refType, String bizType, String oid, List<String> oldNames, List<String> newNames);

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
                if (StringUtils.equals(tag.getType(), tag2.getType()) && StringUtils.equals(tag.getName(), tag2.getName())) {
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

    default void ensureExistOrCreateTag(TagEntity tag) {
        ensureExistOrCreateTagList(Collections.singletonList(tag));
    }


    /**
     * 通过refId包含关系 剔除当前关联 并更新
     * 删除所有 oid
     */
//    void updateTagMappingForRemoveRefId(String oid);

    /**
     * 删除标签、标签关联关系
     * 主要用于动态生成的 tag，如tagName=merchantId，merchant删除时，则清理和该标签所有内容
     */
//    void deleteTagAndTagMapping(List<Long> ids);

    //void deleteTagAndTagMapping(BizType bizType, List<String> tagNameList);

}
