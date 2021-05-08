package org.shoulder.web.template;

import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.log.operation.annotation.OperationLog;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.List;


/**
 * 删除 API
 *
 * @param <ENTITY> 实体
 * @param <ID>     主键
 * @author lym
 */
public interface DeleteController<ENTITY, ID extends Serializable> extends BaseController<ENTITY> {

    /**
     * 删除方法
     *
     * @param ids id
     * @return 是否成功
     */
    @ApiOperation(value = "删除")
    @DeleteMapping
    @OperationLog(operation = OperationLog.Operations.DELETE)
    default BaseResult<Boolean> delete(@RequestBody List<ID> ids) {
        return BaseResult.success(getService().removeByIds(handlerBeforeDelete(ids)));
    }

    /**
     * 删除前置处理
     *
     * @param ids ids
     * @return 返回需要删除的 ids
     */
    default List<ID> handlerBeforeDelete(List<ID> ids) {
        return ids;
    }

}
