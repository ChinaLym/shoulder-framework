package org.shoulder.web.template;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.request.BasePageQuery;
import org.shoulder.core.dto.request.PageQuery;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.log.operation.annotation.OperationLog;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * 查询 API
 *
 * @param <ENTITY>         实体
 * @param <ID>             主键
 * @param <PageQueryPARAM> 查询参数
 * @author lym
 */
public interface QueryController<ENTITY, ID extends Serializable, PageQueryPARAM> extends BaseController<ENTITY> {

    /**
     * 查询
     *
     * @param id 主键id
     * @return 查询结果
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = DataBaseConsts.FIELD_ID, value = "主键", dataType = "long", paramType = "query"),
    })
    @ApiOperation(value = "单体查询", notes = "单体查询")
    @GetMapping("/{id}")
    @OperationLog(operation = OperationLog.Operations.QUERY)
    default BaseResult<ENTITY> get(@PathVariable ID id) {
        return BaseResult.success(getService().getById(id));
    }

    /**
     * 分页查询
     *
     * @param pageQueryParam 分页参数
     * @return 分页数据
     */
    @ApiOperation(value = "分页列表查询")
    @PostMapping(value = "/page")
    @OperationLog(operation = OperationLog.Operations.QUERY)
    default BaseResult<PageResult<ENTITY>> page(@RequestBody @Validated @Nonnull PageQuery<PageQueryPARAM> pageQueryParam) {
        // convert to BO
        BasePageQuery<ENTITY> pageQuery = BasePageQuery.create(pageQueryParam, this::convertToEntity);
        PageResult<ENTITY> queryResult = getService().page(pageQuery);
        return BaseResult.success(queryResult);
    }


    /**
     * 批量查询
     *
     * @param data 批量查询
     * @return 查询结果
     */
    @ApiOperation(value = "批量查询", notes = "批量查询")
    @PostMapping("/query")
    @OperationLog(operation = OperationLog.Operations.QUERY)
    default BaseResult<ListResult<ENTITY>> query(@RequestBody ENTITY data) {
        return BaseResult.success(getService().list(data));
    }

    /**
     * DTO 转 entity
     *
     * @param pageQueryPARAM dto
     * @return entity
     */
    default ENTITY convertToEntity(PageQueryPARAM pageQueryPARAM) {
        return (ENTITY) pageQueryPARAM;
    }


}
