package com.example.demo1.monitor;

import com.example.demo1.BaseWebTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMetrics // 2.4 之后需要加这个 && @SpringBootTest(properties = {"management.endpoints.web.exposure.include=*"})
public class MetricsTest extends BaseWebTest {

    @Test
    public void test0() throws Exception {
        String[] resultContains = {"# HELP demo1_thread_pool_threads",
                "# TYPE demo1_thread_pool_threads gauge",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"max\",} 5.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"active\",} 1.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"largest\",} 1.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"current\",} 1.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"core\",} 5.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"largest\",} 5.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"shoulderThreadPool\",name=\"core\",} 5.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"current\",} 0.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"active\",} 0.0",
                "demo1_thread_pool_threads{appId=\"demo1\",module=\"test\",name=\"max\",} 10.0"
        };

        String content = doGetTest("/actuator/prometheus")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (String except : resultContains) {
            Assertions.assertTrue(content.contains(except));
        }
    }

}
