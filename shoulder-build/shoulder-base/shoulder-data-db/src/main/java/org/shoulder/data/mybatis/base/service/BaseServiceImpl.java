package org.shoulder.data.mybatis.base.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shoulder.data.mybatis.base.dao.IBaseRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 通用业务实现类
 *
 * @author lym
 */
public abstract class BaseServiceImpl<M extends IBaseRepository<T>, T> extends ServiceImpl<M, T> {

    @Autowired
    private IBaseRepository<T> baseRepository;

}
