package com.example.demo2.dto;

import lombok.Data;

/**
 * 测试 bean 转换（DTO、BO等之间转换）
 * @author lym
 */
@Data
public class ShopDTO {

    String id;
    String name;
    String color;
    String address;
    String owner;
    /** 演示目标属性需要根据已有属性计算 */
    String description;
    String createTime;

}
