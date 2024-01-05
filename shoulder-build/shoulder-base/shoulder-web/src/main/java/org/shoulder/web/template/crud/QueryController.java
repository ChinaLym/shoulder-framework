package org.shoulder.web.template.crud;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jakarta.annotation.Nonnull;
import jakarta.validation.Valid;
import org.shoulder.core.dto.request.BasePageQuery;
import org.shoulder.core.dto.request.PageQuery;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.dto.response.PageResult;
import org.shoulder.core.model.Operable;
import org.shoulder.data.constant.DataBaseConsts;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;

/**
 * 查询 API
 *
 * @param <ENTITY>           实体
 * @param <ID>               主键
 * @param <PAGE_QUERY_PARAM> 查询参数
 * @author lym
 */
@Validated
public interface QueryController<ENTITY extends BaseEntity<ID>, ID extends Serializable, PAGE_QUERY_PARAM> extends BaseController<ENTITY> {

    /**
     * 查询
     * service.getById —— mapper.selectById
     *
     * @param id 主键id
     * @return 查询结果
     */
    @ApiImplicitParams({
            @ApiImplicitParam(name = DataBaseConsts.FIELD_ID, value = "主键", dataType = "long", paramType = "path"),
    })
    @ApiOperation(value = "单个查询", notes = "单个查询")
    @GetMapping("/{id}")
    @OperationLog(operation = OperationLog.Operations.QUERY)
    default BaseResult<ENTITY> get(@OperationLogParam @PathVariable(name = "id") ID id) {
        OpLogContextHolder.getLog().setObjectId(String.valueOf(id));
        OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        return BaseResult.success(getService().getById(id));
    }

    /**
     * 分页查询
     * service.page —— mapper.selectPage
     *
     * @param pageQueryParam 分页参数
     * @return 分页数据
     */
    @ApiOperation(value = "分页查询")
    @PostMapping(value = "/page")
    @OperationLog(operation = OperationLog.Operations.QUERY)
    default BaseResult<PageResult<ENTITY>> page(@OperationLogParam @RequestBody @Valid @Nonnull PageQuery<PAGE_QUERY_PARAM> pageQueryParam) {
        // convert to BO
        BasePageQuery<ENTITY> pageQuery = BasePageQuery.create(pageQueryParam, this::convertToEntity);
        OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        PageResult<ENTITY> queryResult = getService().page(pageQuery);
        return BaseResult.success(queryResult);
    }


    /**
     * 批量查询match入参的
     * service.list —— mapper.selectList
     *
     * @param data 批量查询条件
     * @return 查询结果
     */
    @ApiOperation(value = "批量查询", notes = "批量查询")
    @PostMapping("/listAll")
    @OperationLog(operation = OperationLog.Operations.QUERY)
    default BaseResult<ListResult<ENTITY>> listAll(@OperationLogParam @RequestBody ENTITY data) {
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            if (data != null) {
                OpLogContextHolder.setOperableObject((Operable) data);
            }
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        return BaseResult.success(getService().list(data));
    }

    /**
     * DTO 转 entity
     *
     * @param pageQueryParam dto
     * @return entity
     */
    @SuppressWarnings("unchecked")
    default ENTITY convertToEntity(PAGE_QUERY_PARAM pageQueryParam) {
        return (ENTITY) pageQueryParam;
    }


}
