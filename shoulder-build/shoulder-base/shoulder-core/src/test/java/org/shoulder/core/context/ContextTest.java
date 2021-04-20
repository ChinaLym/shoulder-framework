package org.shoulder.core.context;

import org.junit.Test;

public class ContextTest {


    /**
     * 在代码中写中文容易乱码，受编译环境影响，故使用字母来代替
     */
    @Test
    public void testTranslate_zh() {
        String uid = "123";
        AppContext.setUserId(uid);
        assert uid.equals(AppContext.getUserId());
    }


}
