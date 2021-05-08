package org.shoulder.data.mybatis.template.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.shoulder.data.mybatis.template.dao.BaseMapper;
import org.shoulder.data.mybatis.template.entity.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 通用业务实现类
 *
 * @author lym
 */
public abstract class BaseServiceImpl<MAPPER extends BaseMapper<ENTITY>, ENTITY extends BaseEntity<?>> extends ServiceImpl<MAPPER, ENTITY> implements BaseService<ENTITY> {

    /**
     * T 实体的存储层接口
     */
    @Autowired
    private BaseMapper<ENTITY> repository;

    /**
     * 透传
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAllById(ENTITY entity) {
        return SqlHelper.retBool(getBaseMapper().updateAllById(entity));
    }

}
