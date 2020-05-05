package org.shoulder.data.mybatis.base.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.shoulder.data.mybatis.base.dao.IBaseRepository;

/**
 * BaseService
 *
 * @author lym
 */
public abstract class BaseServiceImpl<M extends IBaseRepository<T>, T> extends ServiceImpl<M, T> {
}
