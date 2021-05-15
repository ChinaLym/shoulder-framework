package com.example.demo2.controller.base;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo2.entity.UserEntity;
import com.example.demo2.service.IUserService;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.shoulder.web.template.crud.CrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用 mybatis-plus，基本不需要写基础代码
 *
 * @author lym
 */
@SkipResponseWrap // 跳过包装方便演示
@RestController
@RequestMapping("user")
public class UserController extends CrudController<IUserService, UserEntity, Long, UserEntity, UserEntity, UserEntity> {


    /**
     * 查询 id 为 1 的用户信息
     * http://localhost:8080/user/1
     */
    @RequestMapping("1")
    public UserEntity get() {
        // 已经自动注入 service，即 IUserService
        return service.getById(1);
    }

    /**
     * 查询 name 为 input 的用户信息
     * http://localhost:8080/user/getOne?name=Shoulder
     */
    @RequestMapping("getOne")
    public UserEntity getOne(String name) {
        // 已经自动注入 service，即 IUserService
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getName, name);
        return service.getOne(queryWrapper);
    }


}
