package org.shoulder.data.mybatis.base.controller;


import org.shoulder.data.mybatis.base.service.IBaseService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 基础控制器
 *
 * @author lym
 */
public class BaseController<Biz extends IBaseService<T>, T> {

    @Autowired
    protected Biz bizService;

}
