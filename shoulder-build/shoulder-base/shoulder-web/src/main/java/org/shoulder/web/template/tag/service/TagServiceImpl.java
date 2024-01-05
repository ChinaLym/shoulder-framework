package org.shoulder.web.template.tag.service;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.data.enums.DataErrorCodeEnum;
import org.shoulder.data.mybatis.template.service.BaseCacheableServiceImpl;
import org.shoulder.web.template.tag.dao.TagMapper;
import org.shoulder.web.template.tag.entity.TagEntity;
import org.shoulder.web.template.tag.entity.TagSearchEntity;
import org.shoulder.web.template.tag.repository.TagRelationShipService;
import org.shoulder.web.template.tag.repository.TagRepository;
import org.shoulder.web.template.tag.repository.TagSearchShipService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TagService
 *
 * @author lym
 * @deprecated 暂未实现完善，无法使用
 */
public class TagServiceImpl extends BaseCacheableServiceImpl<TagMapper, TagEntity> implements TagCoreService {

    @Autowired
    private TagRepository tagRepository;

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

    @Override
    public List<TagEntity> searchTagByBizTypeAndName(String type, String searchContent) {
        return tagRepository.lambdaQuery()
                .eq(TagEntity::getBizType, type)
                .likeRight(TagEntity::getName, searchContent)
                .last("limit 50")
                .list();
    }

    @Nullable
    @Override
    public TagEntity queryTagById(Long id) {
        return tagRepository.getById(id);
    }

    @Nullable
    @Override
    public List<TagEntity> queryTagByIdList(List<Long> idList) {
        return tagRepository.listByIds(idList);
    }

    @Nullable
    @Override
    public TagEntity queryTagByBizTypeAndName(String bizType, String name) {
        List<TagEntity> tagEntityList = tagRepository.lambdaQuery()
                .eq(TagEntity::getBizType, bizType)
                .eq(TagEntity::getName, name)
                //.last("limit 50")
                .list();
        return getFirstOrNull(tagEntityList);
    }

    @Override
    public List<TagEntity> queryTagByBizTypeAndNameList(String bizType, List<String> nameList) {
        return tagRepository.lambdaQuery()
                .eq(TagEntity::getBizType, bizType)
                .in(TagEntity::getName, nameList)
                //.last("limit 50")
                .list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Long> queryAllRefIdByStorageSourceAndTagIdList(String refType, List<Long> tagIdList) {
        if (CollectionUtils.isEmpty(tagIdList)) {
            return Collections.emptySet();
        }
        // 限制一下最多标签数量
        AssertUtils.isTrue(tagIdList.size() < 10, DataErrorCodeEnum.DATA_TOO_MUCH);

        List<TagSearchEntity> tagSearchList = tagSearchShipService
                .lambdaQuery()
                .eq(TagSearchEntity::getRefType, refType)
                .in(TagSearchEntity::getRefIds, tagIdList)
                .list();
        if (CollectionUtils.isEmpty(tagSearchList)) {
            return Collections.emptySet();
        }

        Collection<Long>[] refIdsArr = tagSearchList.stream()
                .map(TagSearchEntity::getRefIds).toArray(Set[]::new);
        // 取交集
        Collection<Long> refIds = intersection(refIdsArr);
        AssertUtils.notNull(refIds, CommonErrorCodeEnum.UNKNOWN);
        return new HashSet<>(refIds);
    }


    /**
     * 取交集
     *
     * @param collectionArr 参数
     * @param <T>           范
     * @return 参数全是 null 时才会返回 null，否则非空
     */
    public static <T> Collection<T> intersection(@NotNull Collection<T>... collectionArr) {
        Collection<T> resultSet = null;
        for (Collection<T> collection : collectionArr) {
            if (collection == null) {
                continue;
            }
            if (resultSet == null) {
                resultSet = collection;
            } else {
                resultSet = org.apache.commons.collections4.CollectionUtils.intersection(resultSet, collection);
                if (resultSet.size() == 0) {
                    return Collections.emptySet();
                }
            }
        }
        return resultSet;
    }

    @Override
    public void attachTags(String refType, Long refId, List<TagEntity> tagList) {
        for (TagEntity t : tagList) {
            // TODO lock
            TagSearchEntity tagSearch = null;//tagSearchShipService.lockBy(refType, t.getId());
            if (tagSearch == null) {
                tagSearch = new TagSearchEntity();
                tagSearch.setTagId(t.getId());
                tagSearch.setRefType(refType);
                tagSearch.setRefIds(Collections.singleton(refId));
                tagSearchShipService.save(tagSearch);
            } else if (!tagSearch.getRefIds().contains(refId)) {
                tagSearch.getRefIds().add(refId);
                tagSearchShipService.updateById(tagSearch);
            }
            // 否则是已经有了
        }
    }

    @Override
    public List<TagEntity> attachTags(String refType, Long refId, String bizType,
                                      List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            return new LinkedList<>();
        }
        // 限制一下最多标签数量
        AssertUtils.isTrue(tagNameList.size() < 10, DataErrorCodeEnum.DATA_TOO_MUCH);
        List<TagEntity> allTags = ensureExistOrCreateTag(bizType, tagNameList);
        attachTags(refType, refId, allTags);

        return allTags;
    }

    @Override
    public void removeTagSearches(String refType, Long refId, List<Long> tagIdList) {
        if (CollectionUtils.isEmpty(tagIdList)) {
            return;
        }
        // TODO DUMP CODE
        List<TagSearchEntity> tagSearchList = tagSearchShipService
                .lambdaQuery()
                .eq(TagSearchEntity::getRefType, refType)
                .in(TagSearchEntity::getRefIds, tagIdList)
                .list();
        AssertUtils.notEmpty(tagSearchList, DataErrorCodeEnum.DATA_NOT_EXISTS);
        // 找出标签关联
        for (TagSearchEntity tagSearch : tagSearchList) {
            tagSearch.getRefIds().remove(refId);
            if (tagSearch.getRefIds().isEmpty()) {
                // 这个标签和这类存储再也没有任何关联，可以删除 tagSearch，并尝试清理 tag
                tagSearchShipService.removeById(tagSearch.getId());
                tryDeleteTag(tagSearch.getTagId());
            } else {
                tagSearchShipService.updateById(tagSearch);
            }
        }
    }

    @Override
    public void updateTagSearches(String refType, String bizType, Long refId, String oldName,
                                  String newName) {
        if (StringUtils.isNotBlank(oldName)) {
            //旧的mcc有值，先移除tagSearch表中的值
            TagEntity tag = queryTagByBizTypeAndName(bizType,
                    oldName);
            if (null != tag) {
                List<Long> tags = new ArrayList<>();
                tags.add(tag.getId());
                removeTagSearches(refType, refId, tags);
            }
        }
        if (StringUtils.isNotBlank(newName)) {
            //商服有数据的话，直接插入search表方便后续搜索
            List<String> tagNameList = new ArrayList<>();
            tagNameList.add(newName);
            attachTags(refType, refId, bizType,
                    tagNameList);
        }
    }

    @Override
    public void batchUpdateTagSearches(String refType, String bizType, Long refId, List<String> oldNames,
                                       List<String> newNames) {
        if (CollectionUtils.isNotEmpty(oldNames)) {
            //旧的BizRoleTypes有值，先移除tagSearch表中的值
            for (String oldName : oldNames) {
                TagEntity tag = queryTagByBizTypeAndName(bizType,
                        oldName);
                AssertUtils.notNull(tag, DataErrorCodeEnum.DATA_NOT_EXISTS);
                List<Long> tags = new ArrayList<>();
                tags.add(tag.getId());
                removeTagSearches(refType, refId, tags);
            }
        }
        if (CollectionUtils
                .isNotEmpty(newNames)) {
            for (String newName : newNames) {
                //商服有数据的话，直接插入search表方便后续搜索
                List<String> tagNameList = new ArrayList<>();
                tagNameList.add(newName);
                attachTags(refType, refId,
                        bizType, tagNameList);
            }
        }
    }

    private void tryDeleteTag(Long tagId) {
        TagEntity tag = tagRepository.lockById(tagId);
        AssertUtils.notNull(tag, DataErrorCodeEnum.DATA_NOT_EXISTS);
        TagSearchEntity tagSearch = new TagSearchEntity();
        tagSearch.setTagId(tagId);
        // todo count
        int existCount = 0;//tagSearchShipService.count(query(tagSearch));
        if (existCount == 0) {
            // 当且仅当没有引用才删除
            tagRepository.removeById(tagId);
        }
    }

    /**
     * 确保标签存在
     *
     * @param tagList tagList
     */
    @Override
    public void ensureExistOrCreateTagList(List<TagEntity> tagList) {
        List<Long> tagIdList = tagList.stream()
                .map(TagEntity::getId)
                .collect(Collectors.toList());
        // lock
        List<TagEntity> existsTags = tagRepository.lockByIds(tagIdList);
        Set<Long> existsTagIds = existsTags.stream()
                .map(TagEntity::getId)
                .collect(Collectors.toSet());

        // 过滤出没有的
        List<TagEntity> toSaveTagList = tagList.stream()
                .filter(t -> !existsTagIds.contains(t.getId()))
                .collect(Collectors.toList());

        tagRepository.saveBatch(toSaveTagList);
    }

    public List<TagEntity> ensureExistOrCreateTag(String bizType, List<String> tagNameList) {
        AssertUtils.isTrue(tagNameList.size() < 10, DataErrorCodeEnum.DATA_TOO_MUCH);
        // TODO lock
        List<TagEntity> existsTags = null;//tagRepository.lockById(bizType,  );
        Set<String> existTagNameSet = null == existsTags ? new HashSet<>()
                : new HashSet<>(existsTags.stream().map(TagEntity::getName).collect(Collectors.toSet()));

        // 过滤出没有的
        List<TagEntity> toSaveTagList = tagNameList.stream()
                .filter(name -> !existTagNameSet.contains(name))
                .map(n -> {
                    TagEntity t = new TagEntity();
                    t.setBizType(bizType);
                    t.setName(n);
                    return t;
                }).collect(Collectors.toList());

        tagRepository.saveBatch(toSaveTagList);

        if (CollectionUtils.isNotEmpty(existsTags)) {
            toSaveTagList.addAll(existsTags);
        }
        return toSaveTagList;
    }


}
