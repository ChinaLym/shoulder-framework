package org.shoulder.web.template.crud;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.model.Operable;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.model.sample.OperableObject;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 删除 API
 *
 * 暴露以下接口：
 * DELETE /{bizId}  根据 bizId 删除单个
 * DELETE /         根据 bizIdList 删除多个
 *
 * @param <ENTITY>  实体
 * @param <ID>      主键
 * @author lym
 */
public interface DeleteController<
        ENTITY extends BaseEntity<ID>,
        ID extends Serializable,
        UPDATE_RESULT_DTO extends Serializable>
    extends BaseController<ENTITY> {

    /**
     * 删除单个（有限）
     * service.removeById —— mapper.deleteById
     *
     * @param id id
     * @return 是否成功
     */
    @Operation(summary = "删除", description = "若为 BizEntity 则根据 bizId 软删除，否则根据 id 删除")
    @DeleteMapping("{id}")
    @OperationLog(operation = OperationLog.Operations.DELETE)
    @Transactional(rollbackFor = Exception.class)
    default BaseResult<Boolean> deleteByBizIdOrId(@OperationLogParam @PathVariable("id") String id) {
        String realWantDeleteBizId = handlerBeforeDelete(id);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            OpLogContextHolder.getLog().setObjectId(String.valueOf(realWantDeleteBizId));
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        return BaseResult.success(extendsFromBizEntity() ?
            getService().removeByBizId(realWantDeleteBizId)
            : getService().removeById(getConversionService().convert(realWantDeleteBizId, getEntityIdType())));
    }

    /**
     * 批量删除
     * service.removeByIds —— mapper.deleteBatchIds
     * todo 请求体转为 json 格式，而非 Array
     *
     * @param bizIdList
     * @return 是否成功
     */
    @Operation(summary = "批量删除", description = "若为 BizEntity 则根据 bizId 软删除，否则根据 id 删除")
    @DeleteMapping
    @OperationLog(operation = OperationLog.Operations.DELETE)
    default BaseResult<Boolean> deleteBatchByBizIdOrId(@OperationLogParam @NotEmpty @RequestBody List<String> bizIdList) {
        List<String> realWantDeleteIds = handlerBeforeDelete(bizIdList);
        // todo 优化：所有判断可以拿掉了 if (Operable.class.isAssignableFrom(getEntityClass()))
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            OpLogContextHolder.setOperableObjects(OperableObject.ofIds(realWantDeleteIds));
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        if(extendsFromBizEntity()) {
            // 根据 bizId 软删除
            return BaseResult.success(getService().removeByBizIds(realWantDeleteIds) > 0);
        } else {
            // 根据 id 删除
            List<? extends Serializable> idList = getConversionService().convert(realWantDeleteIds, getEntityIdType());
            return BaseResult.success(getService().removeByIds(idList));
        }

    }

    /**
     * 删除前置处理
     *
     * @param bizIdList bizIdList
     * @return 返回需要删除的 bizIdList
     */
    default List<String> handlerBeforeDelete(List<String> bizIdList) {
        return CollectionUtils.isEmpty(bizIdList) ? bizIdList :
                bizIdList.stream()
                        .map(this::handlerBeforeDelete)
                        .collect(Collectors.toList());
    }

    /**
     * 删除前置处理
     *
     * @param bizId bizId
     * @return 返回需要删除的 bizId
     */
    default String handlerBeforeDelete(String bizId) {
        return bizId;
    }

}
