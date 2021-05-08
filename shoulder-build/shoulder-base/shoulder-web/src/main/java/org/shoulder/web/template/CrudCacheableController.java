package org.shoulder.web.template;

import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.data.mybatis.template.service.BaseCacheableService;
import org.shoulder.log.operation.annotation.OperationLog;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.Serializable;

/**
 * 带缓存的
 * 查询时优先查缓存，并提供了刷新缓存的接口
 *
 * @author lym
 */
public abstract class CrudCacheableController<S extends BaseCacheableService<ENTITY>, ENTITY, Id extends Serializable, PageQuery, SaveDTO, UpdateDTO>
        extends CrudController<S, ENTITY, Id, PageQuery, SaveDTO, UpdateDTO> {

    /**
     * 查询
     *
     * @param id 主键id
     * @return 查询结果
     */
    @Override
    @OperationLog(operation = OperationLog.Operations.QUERY)
    public BaseResult<ENTITY> get(@PathVariable Id id) {
        return BaseResult.success(service.getByIdFromCache(id));
    }


    /**
     * 刷新缓存
     *
     * @return 是否成功
     */
    @ApiOperation(value = "刷新缓存", notes = "刷新缓存")
    @PostMapping("refreshCache")
    @OperationLog(operation = OperationLog.Operations.UPDATE)
    public BaseResult<Boolean> refreshCache() {
        service.refreshCache();
        return BaseResult.success(true);
    }

    /**
     * 清理缓存
     *
     * @return 是否成功
     */
    @ApiOperation(value = "清理缓存", notes = "清理缓存")
    @PostMapping("clearCache")
    @OperationLog(operation = OperationLog.Operations.DELETE)
    public BaseResult<Boolean> clearCache() {
        service.clearCache();
        return BaseResult.success(true);
    }
}
