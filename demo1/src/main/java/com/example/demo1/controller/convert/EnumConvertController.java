package com.example.demo1.controller.convert;

import com.example.demo1.enums.MyColorEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.core.converter.EnumConverter;
import org.shoulder.core.exception.BaseRuntimeException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 可以自动将字符串转为对应的枚举
 *
 * @author lym
 */
@RestController
@RequestMapping("enum")
public class EnumConvertController {

    /**
     * <a href="http://localhost:8080/enum/0?color=RED"/> 输入枚举对应的名称可以成功转换
     *
     * 不优雅的实现，自己写（不推荐）
     */
    @GetMapping("0")
    public MyColorEnum notRecommended(String color){

        MyColorEnum colorEnum = null;

        MyColorEnum[] enums = MyColorEnum.values();
        for (MyColorEnum e : enums) {
            if(e.name().equals(color)){
                // 找到了
                colorEnum = e;
                break;
            }
        }
        if(colorEnum != null){
            System.out.println(colorEnum);
            return colorEnum;
        }
        // 不存在的枚举值
        throw new BaseRuntimeException("0x123246", "参数输入错误");
    }


    /**
     * <a href="http://localhost:8080/enum/1?color=RED"/> 输入枚举对应的名称可以成功转换
     *
     * 默认采用名称严格匹配，也可以实现自己的转换器，实现方式参见 {@link EnumConverter}
     */
    @GetMapping("1")
    public MyColorEnum case1(MyColorEnum color){
        System.out.println(color);
        return color;
    }

    /**
     * 接收多个参数包含枚举  <a href="http://localhost:8080/enum/1?id=123&favoriteColor=RED"/>
     */
    @GetMapping("2")
    public User case2(User user){
        System.out.println(user);
        return user;
    }

    /**
     * 接收请求体包含枚举类型
     */
    @PostMapping("3")
    public User case3(@RequestBody User user){
        System.out.println(user);
        return user;
    }

    /**
     * 自定义类型
     */
    @Data
    public static class User {
        private String id;
        private MyColorEnum favoriteColor;
    }

}
