package com.example.demo1.controller.convert;

import com.example.demo1.bo.ShopBO;
import com.example.demo1.bo.map.DemoBeanMapping;
import com.example.demo1.dto.ShopDTO;
import com.example.demo1.enums.MyColorEnum;
import org.shoulder.core.converter.EnumConverter;
import org.shoulder.core.util.StringUtils;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


/**
 * 原生 MapStruct， shoulder 未做任何修改
 * <p>
 * 介绍 https://gitee.com/ChinaLym/shoulder-framework/tree/master/shoulder-build/shoulder-starters/shoulder-starter-beanmap
 * 介绍 https://github.com/ChinaLym/shoulder-framework/tree/master/shoulder-build/shoulder-starters/shoulder-starter-beanmap
 *
 * @author lym
 */
@SkipResponseWrap
@RestController
@RequestMapping("bean")
public class BeanMapController {

    @Autowired
    private DemoBeanMapping beanMapping;

    /**
     * <a href="http://localhost:8080/bean/1"/> 自动转换，懒人专享
     * <p>
     * 默认采用名称严格匹配，也可以实现自己的转换器，实现方式参见 {@link EnumConverter}
     */
    @GetMapping("1")
    public ShopDTO case1() {
        return beanMapping.toDTO(demoBO());
    }

    /**
     * <a href="http://localhost:8080/bean/2"/> 复杂的转换
     */
    @GetMapping("2")
    public ShopDTO case2() {
        return beanMapping.toComposeDTO(demoBO());
    }

    /**
     * 假设有这么一个 BO
     */
    private ShopBO demoBO() {
        ShopBO bo = new ShopBO();
        bo.setId(StringUtils.uuid32());
        bo.setName("shoulder 杂货铺");
        bo.setColor(MyColorEnum.BLUE);
        bo.setAddr("Beijing");
        ShopBO.Owner owner = new ShopBO.Owner("shoulder", 20);
        bo.setBoss(owner);
        bo.setCreateTime(new Date(System.currentTimeMillis()));
        return bo;
    }

}
