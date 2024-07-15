package org.shoulder.core.concurrent;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadsTest {

    @Test
    public void testDelay() throws InterruptedException {
        Threads.setExecutorService(new ThreadPoolExecutor(2, 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(10)));
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.initialize();
        Threads.setTaskScheduler(scheduler);
        Threads.delay(() -> System.out.println("test"), Duration.ofSeconds(5));

        Thread.sleep(1000 * 10);
    }

}
