package org.shoulder.web.template;

import io.swagger.annotations.ApiOperation;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.log.operation.annotation.OperationLog;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 新增 API
 *
 * @param <ENTITY>   实体
 * @param <SAVE_DTO> DTO
 * @author lym
 */
public interface SaveController<ENTITY, SAVE_DTO> extends BaseController<ENTITY> {

    /**
     * 新增
     *
     * @param dto 保存参数
     * @return ok
     */
    @ApiOperation(value = "新增")
    @PostMapping
    @OperationLog(operation = OperationLog.Operations.CREATE)
    default BaseResult<Void> save(@RequestBody @Validated(BaseEntity.Create.class) SAVE_DTO dto) {
        getService().save(handleBeforeSave(dto));
        return BaseResult.success();
    }

    /**
     * 新增前扩展点
     *
     * @param dto DTO
     * @return entity
     */
    @SuppressWarnings("unchecked")
    default ENTITY handleBeforeSave(SAVE_DTO dto) {
        return (ENTITY) dto;
    }

}
