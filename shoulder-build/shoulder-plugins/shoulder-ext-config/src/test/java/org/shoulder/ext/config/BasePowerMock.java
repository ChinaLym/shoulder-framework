package org.shoulder.ext.config;

import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 *
 */
@RunWith(PowerMockRunner.class)  //使用PowerMockRunner运行时
//@PowerMockIgnore({"javax.management.*"}) //忽略一些mock异常
public class BasePowerMock {


    @org.junit.Test
    public void suppressWarning() {
    }
}