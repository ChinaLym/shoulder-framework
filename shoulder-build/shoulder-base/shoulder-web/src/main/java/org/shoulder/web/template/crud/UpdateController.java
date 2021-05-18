package org.shoulder.web.template.crud;

import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.model.Operable;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.validate.groups.Update;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.function.Function;

/**
 * 修改 API
 *
 * @param <ENTITY>     实体
 * @param <UPDATE_DTO> DTO
 * @author lym
 */
public interface UpdateController<ENTITY extends BaseEntity<? extends Serializable>, UPDATE_DTO> extends BaseController<ENTITY> {

    /**
     * 修改
     * service.updateById —— mapper.updateById
     *
     * @param dto 修改DTO
     * @return 修改后的实体数据
     */
    @ApiOperation(value = "修改", notes = "修改UpdateDTO中不为空的字段")
    @PutMapping
    @OperationLog(operation = OperationLog.Operations.UPDATE)
    @Validated(Update.class)
    default BaseResult<Boolean> update(@OperationLogParam @RequestBody @Valid @NotNull UPDATE_DTO dto) {
        return update(dto, getService()::updateById);
    }

    /**
     * 修改所有字段
     * service.updateAllById —— mapper.updateAllById
     *
     * @param dto 修改DTO
     * @return 修改后的实体数据
     */
    @ApiOperation(value = "修改所有字段", notes = "修改所有字段，没有传递的字段会被置空")
    @PutMapping("/all")
    @OperationLog(operation = OperationLog.Operations.UPDATE)
    @Validated(Update.class)
    default BaseResult<Boolean> updateAll(@OperationLogParam @RequestBody @Valid @NotNull UPDATE_DTO dto) {
        return update(dto, getService()::updateAllById);
    }

    /**
     * 使用 updateMethod 更新 dto
     *
     * @param dto          dto
     * @param updateMethod m
     * @return boolean
     */
    private BaseResult<Boolean> update(UPDATE_DTO dto, Function<ENTITY, Boolean> updateMethod) {
        ENTITY entity = handleBeforeUpdate(dto);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            if (entity != null) {
                OpLogContextHolder.setOperableObject((Operable) entity);
            }
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        return BaseResult.success(updateMethod.apply(entity));
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
