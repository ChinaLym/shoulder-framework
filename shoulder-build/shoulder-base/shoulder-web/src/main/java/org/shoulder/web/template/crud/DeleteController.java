package org.shoulder.web.template.crud;

import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.model.Operable;
import org.shoulder.log.operation.annotation.OperationLog;
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
 * @param <ENTITY> 实体
 * @param <ID>     主键
 * @author lym
 */
public interface DeleteController<ENTITY, ID extends Serializable> extends BaseController<ENTITY> {

    /**
     * 删除单个
     * service.removeById —— mapper.deleteById
     *
     * @param id id
     * @return 是否成功
     */
    @ApiOperation(value = "删除")
    @DeleteMapping("{id}")
    @OperationLog(operation = OperationLog.Operations.DELETE)
    default BaseResult<Boolean> delete(@PathVariable("id") ID id) {
        ID realWantDeleteId = handlerBeforeDelete(id);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            OpLogContextHolder.getLog().setObjectId(String.valueOf(realWantDeleteId));
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        return BaseResult.success(getService().removeById(realWantDeleteId));
    }

    /**
     * 批量删除
     * service.removeByIds —— mapper.deleteBatchIds
     * todo 请求体转为 json 格式，而非 Array
     *
     * @param ids id
     * @return 是否成功
     */
    @ApiOperation(value = "删除")
    @DeleteMapping
    @OperationLog(operation = OperationLog.Operations.DELETE)
    default BaseResult<Boolean> deleteBatch(@RequestBody List<ID> ids) {
        List<ID> realWantDeleteIds = handlerBeforeDelete(ids);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            OpLogContextHolder.setOperableObjects(OperableObject.ofIds(realWantDeleteIds));
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        return BaseResult.success(getService().removeByIds(realWantDeleteIds));
    }

    /**
     * 删除前置处理
     *
     * @param ids ids
     * @return 返回需要删除的 ids
     */
    default List<ID> handlerBeforeDelete(List<ID> ids) {
        return CollectionUtils.isEmpty(ids) ? ids :
                ids.stream()
                        .map(this::handlerBeforeDelete)
                        .collect(Collectors.toList());
    }

    /**
     * 删除前置处理
     *
     * @param ids ids
     * @return 返回需要删除的 ids
     */
    default ID handlerBeforeDelete(ID ids) {
        return ids;
    }

}
