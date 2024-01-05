package org.shoulder.web.template.crud;

import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.model.Operable;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.data.enums.DataErrorCodeEnum;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.entity.BizEntity;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.validate.groups.Create;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;

/**
 * 新增 API
 *
 * @param <ENTITY>   实体
 * @param <SAVE_DTO> DTO
 * @author lym
 */
public interface SaveController<ENTITY extends BaseEntity<? extends Serializable>, SAVE_DTO> extends BaseController<ENTITY> {

    /**
     * 新增
     * service.save —— mapper.insert
     *
     * @param dto 保存参数
     * @return ok
     */
    @ApiOperation(value = "新增")
    @PostMapping
    @OperationLog(operation = OperationLog.Operations.CREATE)
    @Validated(Create.class)
    default BaseResult<Void> save(@OperationLogParam @RequestBody @Valid @NotNull SAVE_DTO dto) {
        ENTITY entity = handleBeforeSave(dto);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            if (entity != null) {
                OpLogContextHolder.setOperableObject(entity);
            }
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        if (entity == null) {
            AssertUtils.notNull(entity, CommonErrorCodeEnum.UNKNOWN);
        }
        if (entity instanceof BizEntity) {
            BizEntity<? extends Serializable> bizEntity = (BizEntity<? extends Serializable>) entity;
            String bizId = generateBizId(entity);
            bizEntity.setBizId(bizId);
            ENTITY dataInDb = getService().lockByBizId(bizEntity.getBizId());
            // 数据不存在
            AssertUtils.isNull(dataInDb, DataErrorCodeEnum.DATA_ALREADY_EXISTS);
        }
        getService().save(entity);
        return BaseResult.success();
    }

    String generateBizId(ENTITY entity);

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
