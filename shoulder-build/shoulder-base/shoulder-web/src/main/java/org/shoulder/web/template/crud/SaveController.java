package org.shoulder.web.template.crud;

import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import io.swagger.annotations.ApiOperation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.model.Operable;
import org.shoulder.core.util.AssertUtils;
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
 * 暴露以下接口：
 * POST /    新建一个，可根据 bizId 或 biz源字段自动幂等
 *
 * @param <ENTITY>   实体
 * @param <SAVE_DTO> DTO
 * @author lym
 */
public interface SaveController<
        ENTITY extends BaseEntity<? extends Serializable>,
        SAVE_DTO extends Serializable,
        SAVE_RESULT_DTO extends Serializable>
    extends BaseController<ENTITY> {

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
    default BaseResult<SAVE_RESULT_DTO> save(@OperationLogParam @RequestBody @Valid @NotNull SAVE_DTO dto) {
        ENTITY entity = handleBeforeSaveAndConvertToEntity(dto);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            if (entity != null) {
                OpLogContextHolder.setOperableObject(entity);
            }
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        AssertUtils.notNull(entity, CommonErrorCodeEnum.ILLEGAL_PARAM);
        if (entity instanceof BizEntity) {
            BizEntity<? extends Serializable> bizEntity = (BizEntity<? extends Serializable>) entity;
            if (bizEntity.getBizId() == null) {
                String bizId = generateBizId(entity);
                bizEntity.setBizId(bizId);
            }
            ENTITY dataInDb = getService().lockByBizId(bizEntity.getBizId());
            // 数据不存在
            AssertUtils.isNull(dataInDb, CommonErrorCodeEnum.DATA_ALREADY_EXISTS);
        }
        getService().save(entity);
        return BaseResult.success(convertEntityToSaveResultDTO(entity));
    }

    String generateBizId(ENTITY entity);

    /**
     * 新增前扩展点
     *
     * @param dto DTO
     * @return entity
     */
    @SuppressWarnings("unchecked")
    default ENTITY handleBeforeSaveAndConvertToEntity(SAVE_DTO dto) {
        return getConversionService().convert(dto, getEntityClass());
    }

    @SuppressWarnings("unchecked")
    default Class<SAVE_RESULT_DTO> getSaveResultDTOClass() {
        return (Class<SAVE_RESULT_DTO>) GenericTypeUtils.resolveTypeArguments(this.getClass(), SaveController.class)[2];
    }

    default SAVE_RESULT_DTO convertEntityToSaveResultDTO(ENTITY entity) {
        return getConversionService().convert(entity, getSaveResultDTOClass());
    }

}
