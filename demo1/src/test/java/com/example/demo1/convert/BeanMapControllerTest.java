package com.example.demo1.convert;

import com.example.demo1.BaseControllerTest;
import org.junit.jupiter.api.Test;

class BeanMapControllerTest extends BaseControllerTest {

    @Test
    public void test1() throws Exception {
        doGetTest("/bean/1",
                "\",\"name\":\"shoulder 杂货铺\",\"color\":\"BLUE\",\"address\":null,\"owner\":null,\"description\":null,\"createTime\":");
    }

    @Test
    public void test2() throws Exception {
        doGetTest("/bean/2",
                "\",\"name\":\"shoulder 杂货铺\",\"color\":\"BLUE\",\"address\":\"Beijing\",\"owner\":\"shoulder\",\"description\":\"this shop owned is in Beijing, and boss is shoulder.\",\"createTime\":\"");
    }

}
