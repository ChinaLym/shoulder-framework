package com.example.demo1.convert;

import com.example.demo1.BaseControllerTest;
import com.example.demo1.controller.convert.EnumConvertController;
import com.example.demo1.enums.MyColorEnum;
import org.junit.jupiter.api.Test;

import java.util.Map;

class EnumConvertTest extends BaseControllerTest {


    @Test
    public void test1() throws Exception {
        String result = "{\"code\":\"0\",\"msg\":\"success\",\"data\":\"RED\"";
        doGetTest("/enum/0?color=RED", result);
        doGetTest("/enum/1?color=RED", result);
    }


    @Test
    public void test2() throws Exception {
        doGetTest("/enum/1?id=123&favoriteColor=RED",
                "{\"code\":\"0\",\"msg\":\"success\",\"data\":null");
    }

    @Test
    public void test3() throws Exception {
        EnumConvertController.User user = new EnumConvertController.User();
        user.setId("123");
        user.setFavoriteColor(MyColorEnum.BLUE);
        doPostTest("/enum/3", Map.of("user", user), "");
    }

}
