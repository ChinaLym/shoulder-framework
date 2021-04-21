package com.example.demo3.token.mvc;

import com.example.demo3.token.BaseWebTest;
import org.junit.jupiter.api.Test;

public class MvcTest extends BaseWebTest {

    @Test
    public void needLoginFirst() throws Exception {
        doGetTest("/", "{\"code\":\"0x0000000d\",\"msg\":\"Certification expired. Re-auth please.\",\"data\":null}");
    }


}
