package com.example.demo1.convert;

import com.example.demo1.BaseWebTest;
import org.junit.jupiter.api.Test;

class BeanMapTest extends BaseWebTest {

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
