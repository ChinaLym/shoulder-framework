package com.example.demo1.bo.map;

import com.example.demo1.bo.ShopBO;
import com.example.demo1.dto.ShopDTO;
import org.mapstruct.Mapper;

/**
 * @author lym
 */
@Mapper(componentModel = "spring") // 置为 spring 则默认注入 bean
public interface DemoBeanMapping {

    /**
     * 用来获取实例，若开发中用 Spring，则可不写
     */
    //ShopMapping shopMapping = Mappers.getMapper(ShopMapping.class);

    /**
     * 若类型、属性名相同则不用写 Mapping，其中注解也可以不用谢
     *
     * @param bo
     * @return
     */
    //@Mapping(target = "color", source = "color.name")
    ShopDTO toShopDTO(ShopBO bo);


}