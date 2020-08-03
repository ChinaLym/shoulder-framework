package com.example.demo1.controller.convert;

import com.example.demo1.bo.ShopBO;
import com.example.demo1.dto.ShopDTO;
import com.example.demo1.bo.map.DemoBeanMapping;
import com.example.demo1.enums.MyColorEnum;
import org.shoulder.core.converter.EnumConverter;
import org.shoulder.core.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lym
 */
@RestController
@RequestMapping("bean")
public class BeanMapController {

    @Autowired
    private DemoBeanMapping beanMapping;

    /**
     * <a href="http://localhost:8080/bean/1"/> 输入枚举对应的名称可以成功转换
     *
     * 默认采用名称严格匹配，也可以实现自己的转换器，实现方式参见 {@link EnumConverter}
     */
    @GetMapping("1")
    public ShopDTO case1(){
        ShopBO bo = new ShopBO();
        bo.setId(StringUtils.uuid32());
        bo.setName(StringUtils.uuid32());
        bo.setColor(MyColorEnum.BLUE);
        return beanMapping.toShopDTO(bo);
    }

}
