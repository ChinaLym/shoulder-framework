package com.example.demo2.controller.base;

import com.example.demo2.entity.UserEntity;
import com.example.demo2.service.IUserService;
import org.shoulder.web.template.crud.CrudController;
import org.shoulder.web.template.crud.DeleteController;
import org.shoulder.web.template.crud.QueryController;
import org.shoulder.web.template.crud.SaveController;
import org.shoulder.web.template.crud.UpdateController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 使用 mybatis-plus，基本不需要写基础代码
 * <p>
 * 查询 id 为 1 的用户信息
 * http://localhost:8080/user/1
 * <p>
 * POST http://localhost:8080/user/listAll 【注意POST】todo 这类如何快捷访问
 * POST http://localhost:8080/user/page 【注意POST】
 * BODY {}
 *
 * @author lym
 *
 * extends {@link CrudController} 会暴露哪些接口？ 见其继承的接口
 * @see QueryController
 * @see SaveController
 * @see UpdateController
 * @see DeleteController
 */
@RestController
@RequestMapping("user")
public class UserController extends CrudController<IUserService, UserEntity, Long, UserEntity, UserEntity, UserEntity> {


}
