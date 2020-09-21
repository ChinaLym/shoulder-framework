package com.example.demo1.bo.map;

import com.example.demo1.bo.ShopBO;
import com.example.demo1.dto.ShopDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 高性能的自定義 bean 轉換工具，只需要写接口参数和返回值类型就可以了，一般认为性能是 dozer 的百倍以上
 * 入门 https://blog.csdn.net/zhige_me/article/details/80699784
 *
 * @author lym
 */
@Mapper(componentModel = "spring") // 置为 spring 则默认注入 bean
public interface DemoBeanMapping {

    /**
     * 用来获取实例，若开发中用 Spring，则可不写
     */
    //ShopMapping shopMapping = Mappers.getMapper(ShopMapping.class);

    /**
     * 快速转换，懒人必备
     * 若类型、属性名相同则不用写 @Mapping（枚举-字符串也不用）
     *
     * @param bo 源
     * @return 目标 DTO
     */
    ShopDTO toDTO(ShopBO bo);

    /**
     * 复杂转换
     * 若类型、属性名相同则不用写 Mapping，其中注解也可以不用谢
     *
     * @param bo 源
     * @return 目标 DTO
     */
    @Mapping(source = "addr", target = "address")
    @Mapping(source = "boss.name", target = "owner")
    @Mapping(expression = "java(\"this shop owned is in \" + bo.getAddr() + \", and boss is \" + bo.getBoss().getName() + \".\")", target = "description")
    @Mapping(source = "createTime", target = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    ShopDTO toComposeDTO(ShopBO bo);


}