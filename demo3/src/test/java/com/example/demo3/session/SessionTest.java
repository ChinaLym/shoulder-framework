package com.example.demo3.session;

import com.example.demo3.BaseWebTest;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import org.shoulder.core.util.ArrayUtils;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SessionTest extends BaseWebTest {

    // session
    @Test
    public void needLoginFirstOrRedirectTest() throws Exception {
        if (!isSessionMode()) {
            return;
        }

        doGetTest("/")
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", Is.is("http://localhost/signIn.html")));


    }

    protected boolean isSessionMode() {
        return ArrayUtils.contains(environment.getActiveProfiles(), "session");
    }

}
