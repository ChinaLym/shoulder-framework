package com.example.demo1.bo;

import com.example.demo1.dto.ShopDTO;
import com.example.demo1.enums.MyColorEnum;
import lombok.Data;
import org.mapstruct.Mapper;

/**
 * @author lym
 */
@Data
public class ShopBO {

    String id;
    String name;
    MyColorEnum color;

}
