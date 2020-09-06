package com.example.demo2.bo;

import com.example.demo2.enums.MyColorEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 测试 bean 转换（DTO、BO等之间转换）
 *
 * @author lym
 */
@Data
public class ShopBO {

    String id;
    String name;
    /** 枚举转字符串，默认使用其名称 */
    MyColorEnum color;
    /** 属性名不同需要在 @Mapping 中描述 */
    String addr;
    Owner boss;
    Date createTime;
    /**
     * 超市拥有者，演示复杂类型转换
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Owner {
        String name;
        int age;
    }

}
