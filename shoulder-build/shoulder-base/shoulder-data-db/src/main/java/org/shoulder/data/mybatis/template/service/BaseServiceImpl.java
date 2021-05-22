package org.shoulder.data.mybatis.template.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.shoulder.data.mybatis.template.entity.BizEntity;
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
     * 注意需要在调用该方法处加锁，判断是否存在
     *
     * @param entity e
     * @return b
     */
    @Override
    public boolean save(ENTITY entity) {
        return super.save(entity);
    }

    /**
     * 透传
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAllById(ENTITY entity) {
        return SqlHelper.retBool(getBaseMapper().updateAllFieldsById(entity));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean saveOrUpdate(ENTITY entity) {
        if (entity instanceof BizEntity) {
            String bizId = ((BizEntity) entity).getBizId();
            if (bizId == null) {
                // 补充 bizId？，默认抛异常
                throw new IllegalStateException("bizId == null");
            }
            return lockByBizId(bizId) != null ? updateByBizId(entity) : save(entity);
        } else {
            return super.saveOrUpdate(entity);
        }
    }

}
