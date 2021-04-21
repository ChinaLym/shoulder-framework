package com.example.demo1.monitor;

import com.example.demo1.BaseControllerTest;
import org.junit.jupiter.api.Test;

public class MetricsTest extends BaseControllerTest {

    @Test
    public void test0() throws Exception {
        String result = "# HELP demo1_thread_pool_threads  \n" +
                "# TYPE demo1_thread_pool_threads gauge\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"max\",} 5.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"active\",} 1.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"largest\",} 1.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"current\",} 1.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"core\",} 5.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"largest\",} 5.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"core\",} 5.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"current\",} 0.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"active\",} 0.0\n" +
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"max\",} 10.0";
        doGetTest("/actuator/prometheus", result);
    }

}
