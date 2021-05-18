package org.shoulder.data.mybatis.template.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * 通用业务实现类
 *
 * @author lym
 */
public abstract class BaseServiceImpl<MAPPER extends BaseMapper<ENTITY>, ENTITY extends BaseEntity<? extends Serializable>> extends ServiceImpl<MAPPER, ENTITY> implements BaseService<ENTITY> {

    /**
     * 覆盖了父类方法，有问题直接抛异常，对于 bizEntity 默认根据 bizId 更新
     *
     * @param entity e
     * @return b
     */
    @Override
    public boolean save(ENTITY entity) {
        /*if(entity == null) {
            AssertUtils.notNull(entity, CommonErrorCodeEnum.UNKNOWN);
        }
        if (entity instanceof BizEntity) {
            BizEntity bizEntity = (BizEntity) entity;
            ENTITY dataInDb = getBaseMapper().selectForUpdateByBizId(bizEntity.getBizId());
            // 数据不存在
            AssertUtils.isNull(dataInDb, DataErrorCodeEnum.DATA_ALREADY_EXISTS);
        }*/
        return super.save(entity);
    }

    /**
     * 透传
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAllById(ENTITY entity) {
        return SqlHelper.retBool(getBaseMapper().updateAllById(entity));
    }

}
