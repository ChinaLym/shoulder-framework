package com.example.demo3.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo3.entity.UserEntity;
import com.example.demo3.service.IUserService;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.shoulder.web.template.crud.CrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        // 自动根据当前 Controller 泛型注入对应的 IService（IUserService），可通过 service 调用
        return service.getById(1);
    }

    /**
     * 查询 name 为 input 的用户信息
     * http://localhost:8080/user/getOne?name=Shoulder
     */
    @RequestMapping("getOne")
    public UserEntity getOne(@RequestParam("name") String name) {
        // 已经自动注入 service，即 IUserService
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getName, name);
        return service.getOne(queryWrapper);
    }


}
