package org.shoulder.web.template.crud;

import com.baomidou.mybatisplus.core.toolkit.reflect.GenericTypeUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.model.Operable;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.entity.BizEntity;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.validate.groups.Update;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 修改 API
 * 暴露以下接口：
 * PUT /      更新单个 byId
 * PUT /all   条件更新
 *
 * @param <ENTITY>     实体
 * @param <UPDATE_DTO> DTO
 * @author lym
 */
public interface UpdateController<
        ENTITY extends BaseEntity<? extends Serializable>,
        UPDATE_DTO extends Serializable,
        UPDATE_RESULT_DTO extends Serializable>
    extends BaseController<ENTITY> {

    /**
     * 修改
     * service.updateById —— mapper.updateById
     *
     * @param dto 修改DTO
     * @return 修改后的实体数据
     */
    @Operation(summary = "修改", description = "修改UpdateDTO中不为空的字段，若")
    @PutMapping
    @OperationLog(operation = OperationLog.Operations.UPDATE)
    @Validated(Update.class)
    @Transactional(rollbackFor = Exception.class)
    default BaseResult<UPDATE_RESULT_DTO> updateByBizIdOrId(@OperationLogParam @RequestBody @Valid @NotNull UPDATE_DTO dto) {
        ENTITY entity = handleBeforeUpdateAndConvertToEntity(dto);
        String bizId = null;
        // 如果可用 bizId 则优先用 bizId，否则将根据id更新（id非空）
        boolean useBizId = extendsFromBizEntity() && StringUtils.isNoneBlank(bizId = ((BizEntity<?>) entity).getBizId());
        AssertUtils.isTrue(useBizId || entity.getId() != null, CommonErrorCodeEnum.ILLEGAL_PARAM);

        boolean success = update(dto, useBizId ? getService()::updateByBizId : getService()::updateById);
        AssertUtils.isTrue(success, CommonErrorCodeEnum.DATA_STORAGE_FAIL);

        ENTITY updated = useBizId ? getService().getByBizId(bizId)
            : getService().getById(entity.getId());
        return BaseResult.success(handleAfterUpdateAndConvertToDTO(updated));
    }

    /**
     * 修改所有字段【适用场景主要为安全环境下的后台应用，默认不启用】
     * service.updateAllById —— mapper.updateAllById
     *
     * @param dto 修改DTO
     * @return 修改后的实体数据
     */
//    @Operation(summary = "修改所有字段", description = "修改所有字段，没有传递的字段会被置空")
//    @PutMapping("/allFields")
//    @OperationLog(operation = OperationLog.Operations.UPDATE)
//    @Validated(Update.class)
    default BaseResult<Void> updateAllFieldsByBizId(@OperationLogParam @RequestBody @Valid @NotNull UPDATE_DTO dto) {
//        boolean updateAll = update(dto, getService()::updateBatchByBizId);
        // 暂不支持
        return BaseResult.error(CommonErrorCodeEnum.CODING);
    }

    /**
     * 使用 updateMethod 更新 dto
     *
     * @param dto          dto
     * @param updateMethod m
     * @return boolean
     */
    private boolean update(UPDATE_DTO dto, Function<ENTITY, Boolean> updateMethod) {
        ENTITY entity = handleBeforeUpdateAndConvertToEntity(dto);
        if (Operable.class.isAssignableFrom(getEntityClass())) {
            if (entity != null) {
                OpLogContextHolder.setOperableObject(entity);
            }
            OpLogContextHolder.getLog().setObjectType(getEntityObjectType());
        }
        return updateMethod.apply(entity);
    }

    /**
     * 更新前扩展点（模型转换）
     * 直接强转
     *
     * @param dto DTO
     * @return entity
     */
    default ENTITY handleBeforeUpdateAndConvertToEntity(UPDATE_DTO dto) {
        return getConversionService().convert(dto, getEntityClass());
    }

    /**
     * 更新前扩展点（模型转换）
     * 直接强转
     *
     * @param dto DTO
     * @return entity
     */
    default UPDATE_RESULT_DTO handleAfterUpdateAndConvertToDTO(ENTITY entity) {
        return getConversionService().convert(entity, getUpdateResultDtoClass());
    }

    @SuppressWarnings("unchecked")
    default Class<UPDATE_RESULT_DTO> getUpdateResultDtoClass() {
        return (Class<UPDATE_RESULT_DTO>) GenericTypeUtils.resolveTypeArguments(this.getClass(), UpdateController.class)[2];
    }
}
