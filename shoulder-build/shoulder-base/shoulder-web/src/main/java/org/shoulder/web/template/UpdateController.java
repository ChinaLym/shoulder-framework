package org.shoulder.web.template;

import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.log.operation.annotation.OperationLog;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 修改 API
 *
 * @param <ENTITY>     实体
 * @param <UPDATE_DTO> DTO
 * @author lym
 */
public interface UpdateController<ENTITY, UPDATE_DTO> extends BaseController<ENTITY> {

    /**
     * 修改
     *
     * @param dto 修改DTO
     * @return 修改后的实体数据
     */
    @ApiOperation(value = "修改", notes = "修改UpdateDTO中不为空的字段")
    @PutMapping
    @OperationLog(operation = OperationLog.Operations.UPDATE)
    default BaseResult<Boolean> update(@RequestBody @Validated(BaseEntity.Update.class) UPDATE_DTO dto) {
        return BaseResult.success(getService().updateById(handleBeforeUpdate(dto)));
    }

    /**
     * 修改所有字段
     */
    @ApiOperation(value = "修改所有字段", notes = "修改所有字段，没有传递的字段会被置空")
    @PutMapping("/all")
    @OperationLog(operation = OperationLog.Operations.UPDATE)
    default BaseResult<Boolean> updateAll(@RequestBody @Validated(BaseEntity.Update.class) UPDATE_DTO dto) {
        return BaseResult.success(getService().updateAllById(handleBeforeUpdate(dto)));
    }

    /**
     * 更新前扩展点（模型转换）
     * 直接强转
     *
     * @param dto DTO
     * @return entity
     */
    @SuppressWarnings("unchecked")
    default ENTITY handleBeforeUpdate(UPDATE_DTO dto) {
        return (ENTITY) dto;
    }
}
