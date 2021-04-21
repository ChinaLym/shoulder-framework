package com.example.demo1.ex;

import com.example.demo1.BaseWebTest;
import org.junit.jupiter.api.Test;

public class ExceptionTest extends BaseWebTest {


    @Test
    public void test0() throws Exception {
        doGetTest("/exception/0?kind=1",
                "{\"code\":\"0x000a01\",\"msg\":\"demo ex1\",\"data\":null");
        doGetTest("/exception/0?kind=2",
                "{\"code\":\"0x000a02\",\"msg\":\"demo ex2\",\"data\":null");
    }


    @Test
    public void test1() throws Exception {
        doGetTest("/exception/1?kind=1",
                "{\"code\":\"0x000a01\",\"msg\":\"demo ex1\",\"data\":null");
        doGetTest("/exception/1?kind=2",
                "{\"code\":\"0x000a02\",\"msg\":\"demo ex2\",\"data\":null");
    }

}
