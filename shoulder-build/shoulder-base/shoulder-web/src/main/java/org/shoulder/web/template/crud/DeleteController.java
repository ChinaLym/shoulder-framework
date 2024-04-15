package org.shoulder.web.template.crud;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.model.Operable;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.model.sample.OperableObject;
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
     * 删除单个
     * service.removeById —— mapper.deleteById
     *
     * @param bizId bizId
     * @return 是否成功
     */
    @Operation(summary = "删除")
    @DeleteMapping("{bizId}")
    @OperationLog(operation = OperationLog.Operations.DELETE)
    default BaseResult<Boolean> deleteByBizId(@OperationLogParam @PathVariable("bizId") String bizId) {
        String realWantDeleteBizId = handlerBeforeDelete(bizId);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            OpLogContextHolder.getLog().setObjectId(String.valueOf(realWantDeleteBizId));
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        return BaseResult.success(getService().removeByBizId(realWantDeleteBizId));
    }

    /**
     * 批量删除
     * service.removeByIds —— mapper.deleteBatchIds
     * todo 请求体转为 json 格式，而非 Array
     *
     * @param bizIdList
     * @return 是否成功
     */
    @Operation(summary = "删除")
    @DeleteMapping
    @OperationLog(operation = OperationLog.Operations.DELETE)
    default BaseResult<Boolean> deleteBatchByBizId(@OperationLogParam @RequestBody List<String> bizIdList) {
        List<String> realWantDeleteIds = handlerBeforeDelete(bizIdList);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            OpLogContextHolder.setOperableObjects(OperableObject.ofIds(realWantDeleteIds));
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        return BaseResult.success(getService().removeByBizIds(realWantDeleteIds) > 0);
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
