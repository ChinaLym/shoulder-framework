package org.shoulder.web.template.tag.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import jakarta.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.dto.request.BasePageQuery;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.data.enums.DataErrorCodeEnum;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.service.BaseCacheableServiceImpl;
import org.shoulder.data.uid.BizIdGenerator;
import org.shoulder.web.template.tag.mapper.TagMapper;
import org.shoulder.web.template.tag.model.TagEntity;
import org.shoulder.web.template.tag.model.TagMappingEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TagService
 *
 * @author lym
 */
public class TagServiceImpl extends BaseCacheableServiceImpl<TagMapper, TagEntity> implements TagCoreService {

    @Autowired(required = false)
    private TagMappingService tagMappingService;


    @Autowired(required = false)
    protected BizIdGenerator bizIdGenerator;

    @Transactional(rollbackFor = Exception.class)
    public boolean createTags(List<TagEntity> tagEntityList) {
        // max 100
        AssertUtils.isTrue(tagEntityList.size() < 100, DataErrorCodeEnum.DATA_TOO_MUCH);

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

    @Transactional(rollbackFor = Exception.class)
    public boolean attachTags(String refType, String refObjectId, List<Long> tagIds) {
        // max 20
        AssertUtils.notEmpty(tagIds, CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.isTrue(tagIds.size() < 20, CommonErrorCodeEnum.ILLEGAL_PARAM);

        // 确认数据库中标签存在
        List<TagEntity> tagListInDb = super.listByIds(tagIds);
        Set<Long> tagIdsInDb = tagListInDb.stream()
                .map(TagEntity::getId)
                .collect(Collectors.toSet());
        boolean someTagsNotExist = tagIds.stream()
                .map(tagIdsInDb::contains)
                .anyMatch(contain -> !contain);
        AssertUtils.isFalse(someTagsNotExist, CommonErrorCodeEnum.ILLEGAL_PARAM);

        // for 循环创建标签关联关系（通过多次IO减少单次锁的范围）
        for (Long tagId : tagIds) {
            attachTag(refType, refObjectId, tagId);
        }
        return true;
    }

    // max 100 batch｜ insert
    public boolean attachTag(String refType, String refObjectId, Long tagId) {
        // select for update tagId 对应的标签关系
        TagEntity tagInDb = super.getBaseMapper().selectForUpdateById(tagId);
        AssertUtils.notNull(tagInDb, CommonErrorCodeEnum.DATA_ALREADY_EXISTS);

        TagMappingEntity tagMappingInDb = tagMappingService.getBaseMapper().selectForUpdate(tagId, refType, refObjectId);
        if (tagMappingInDb != null) {
            // 有则返回
            return true;
        }
        // 没有则创建标签
        TagMappingEntity tagMappingEntity = new TagMappingEntity();
        tagMappingEntity.setRefType(refType);
        tagMappingEntity.setTagId(tagId);
        tagMappingEntity.setOid(refObjectId);
        return tagMappingService.save(tagMappingEntity);
    }


    PageResult<TagMappingEntity> searchByTagId(BasePageQuery<TagMappingEntity> pageQueryCondition) {
        // 查标签id，获取外部业务表 bizId
        AssertUtils.notNull(pageQueryCondition, CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notNull(pageQueryCondition.getCondition(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        Long tagId = pageQueryCondition.getCondition().getTagId();
        AssertUtils.notNull(tagId, CommonErrorCodeEnum.ILLEGAL_PARAM);
        AssertUtils.notNull(pageQueryCondition.getCondition().getRefType(), CommonErrorCodeEnum.ILLEGAL_PARAM);
        TagEntity tagEntity = super.getById(tagId);
        AssertUtils.notNull(tagEntity, CommonErrorCodeEnum.DATA_ALREADY_EXISTS);
        // TODO P2 根据标签 order排序？
        return tagMappingService.page(pageQueryCondition);
    }

    //List<String> searchByAllTagIds(List<Long> ids);

    //List<String> searchByAnyTagIds(List<Long> ids);

    @Override
    public List<TagEntity> searchTagByBizTypeAndName(String tagType, String searchContent, Integer limit) {
        LambdaQueryChainWrapper<TagEntity> query = super.lambdaQuery()
                .eq(TagEntity::getType, tagType)
                .likeRight(TagEntity::getName, searchContent);
        query = limit != null ? query.last("limit " + limit) : query;
        return query.list();
    }

    @Nullable
    @Override
    public TagEntity queryTagById(Long id) {
        return super.getById(id);
    }

    @Nullable
    @Override
    public List<TagEntity> queryTagByIdList(List<Long> idList) {
        if (CollectionUtil.isEmpty(idList)) {
            return Collections.emptyList();
        }
        return super.listByIds(idList);
    }

    @Nullable
    @Override
    public TagEntity queryTagByBizTypeAndName(String tagType, String name) {
        List<TagEntity> tagEntityList = super.lambdaQuery()
                .eq(TagEntity::getType, tagType)
                .eq(TagEntity::getName, name)
                .last("limit 1")
                .list();
        return getFirstOrNull(tagEntityList);
    }

    @Override
    public List<TagEntity> queryAllTagByBizTypeAndNameList(String tagType, List<String> nameList) {
        return super.lambdaQuery()
                .eq(TagEntity::getType, tagType)
                .in(TagEntity::getName, nameList)
                .last("limit " + nameList.size())
                .list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<TagMappingEntity> queryAllRefIdByRefTypeAndTagIdList(String refType, List<Long> tagIdList) {
        if (CollectionUtils.isEmpty(tagIdList)) {
            return Collections.emptyList();
        }
        // 限制一下最多标签数量
        AssertUtils.isTrue(tagIdList.size() < 10, DataErrorCodeEnum.DATA_TOO_MUCH);

        return tagMappingService
                .lambdaQuery()
                .eq(TagMappingEntity::getRefType, refType)
                .in(TagMappingEntity::getTagId, tagIdList)
                .list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TagEntity> attachTags(String refType, String refObjectId, String tagType,
                                      List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            return new LinkedList<>();
        }
        // 限制一下最多标签数量
        AssertUtils.isTrue(tagNameList.size() < 10, DataErrorCodeEnum.DATA_TOO_MUCH);
        List<TagEntity> allTags = ensureExistOrCreateTag(tagType, tagNameList);
        attachTags(refType, refObjectId, allTags.stream().map(BaseEntity::getId).collect(Collectors.toList()));

        return allTags;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeTagMappings(String refType, String refObjectId, List<Long> tagIdList) {
        if (CollectionUtils.isEmpty(tagIdList)) {
            return;
        }
        // 批量锁定
        List<TagMappingEntity> tagMappingListInDb = tagMappingService
                .lambdaQuery()
                .eq(TagMappingEntity::getRefType, refType)
                .eq(TagMappingEntity::getOid, refObjectId)
                .in(TagMappingEntity::getTagId, tagIdList)
                .last("for update").list();

        // 确认数据都在
        List<Long> mappingIdList = tagMappingListInDb.stream().map(BaseEntity::getId).collect(Collectors.toList());
        AssertUtils.equals(tagIdList.size(), mappingIdList.size(), CommonErrorCodeEnum.DATA_ALREADY_EXISTS);

        // 删掉
        tagMappingService.getBaseMapper().deleteInLogicByIdList(mappingIdList);
    }

    @Override
    public void updateTagMappings(String refType, String refObjectId, String tagType, String oldTagName, String newTagName) {
        // todo
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateTagMappings(String refType, String refObjectId, String tagType, List<String> oldTagNameList,
                                       List<String> newTagNameList) {
        if (CollectionUtils.isNotEmpty(oldTagNameList)) {
            // oldTagNameList 有值，先移除TagMapping表中的值
            List<Long> tags = new ArrayList<>();
            for (String oldName : oldTagNameList) {
                TagEntity tag = queryTagByBizTypeAndName(tagType, oldName);
                AssertUtils.notNull(tag, CommonErrorCodeEnum.DATA_ALREADY_EXISTS);
                tags.add(tag.getId());
            }
            removeTagMappings(refType, refObjectId, tags);
        }
        if (CollectionUtils.isNotEmpty(newTagNameList)) {
            for (String newName : newTagNameList) {
                //商服有数据的话，直接插入search表方便后续搜索
                List<String> tagNameList = new ArrayList<>();
                tagNameList.add(newName);
                attachTags(refType, refObjectId, tagType, tagNameList);
            }
        }
    }

    private void tryDeleteTag(Long tagId) {
        // todo P2【优化】可以用 lockInShare Mode 提高并发
        TagEntity tag = super.lockById(tagId);
        AssertUtils.notNull(tag, CommonErrorCodeEnum.DATA_ALREADY_EXISTS);
        TagMappingEntity tagMapping = new TagMappingEntity();
        tagMapping.setTagId(tagId);
        long existCount = tagMappingService.count(tagMappingService.query(tagMapping, null));
        if (existCount == 0) {
            // 当且仅当没有引用才删除
            super.removeById(tagId);
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
        List<TagEntity> existsTags = super.lockByIds(tagIdList);
        Set<Long> existsTagIds = existsTags.stream()
                .map(TagEntity::getId)
                .collect(Collectors.toSet());

        // 过滤出没有的
        List<TagEntity> toSaveTagList = tagList.stream()
                .filter(t -> !existsTagIds.contains(t.getId()))
                .collect(Collectors.toList());

        super.saveBatch(toSaveTagList);
    }

    public List<TagEntity> ensureExistOrCreateTag(String type, List<String> tagNameList) {
        AssertUtils.isTrue(tagNameList.size() < 10, DataErrorCodeEnum.DATA_TOO_MUCH);
        List<TagEntity> existsTags = super.getBaseMapper().lockByTypeAndNameList(type, tagNameList);
        Set<String> existTagNameSet = null == existsTags ? new HashSet<>()
                : new HashSet<>(existsTags.stream().map(TagEntity::getName).collect(Collectors.toSet()));

        // 过滤出没有的
        List<TagEntity> toSaveTagList = tagNameList.stream()
                .filter(name -> !existTagNameSet.contains(name))
                .map(n -> {
                    TagEntity t = new TagEntity();
                    t.setType(type);
                    t.setName(n);
                    t.setDisplayName(n);
                    t.setDisplayOrder(1000);
                    t.setSource("CodingAutoGeneratePowerByShoulder");
                    t.setTenant(AppContext.getTenantCode());
                    t.setBizId(bizIdGenerator.generateBizId(t, TagEntity.class));
                    return t;
                }).collect(Collectors.toList());

        super.saveBatch(toSaveTagList);

        if (CollectionUtils.isNotEmpty(existsTags)) {
            toSaveTagList.addAll(existsTags);
        }
        return toSaveTagList;
    }

    // fixme 删除校验 mapping 为0

}
