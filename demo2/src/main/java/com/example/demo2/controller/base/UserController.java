package com.example.demo2.controller.base;

import com.example.demo2.entity.UserEntity;
import com.example.demo2.service.IUserService;
import org.shoulder.web.template.crud.CrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用 mybatis-plus，基本不需要写基础代码
 * <p>
 * 查询 id 为 1 的用户信息
 * http://localhost:8080/user/1
 * <p>
 * POST http://localhost:8080/user/listAll
 * BODY {}
 *
 * @author lym
 */
@RestController
@RequestMapping("user")
public class UserController extends CrudController<IUserService, UserEntity, Long, UserEntity, UserEntity, UserEntity> {


}
