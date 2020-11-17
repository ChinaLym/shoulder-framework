package org.shoulder.core.lock;

import org.junit.Test;
import org.shoulder.core.lock.impl.MemoryLock;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LockTest {

    /**
     * 测试内存锁
     */
    @Test
    public void testMemoryLock() throws InterruptedException {
        testExclusion(new MemoryLock());
    }


    /**
     * 互斥性测试
     *
     * @param lock lock
     */
    public void testExclusion(ServerLock lock) throws InterruptedException {
        int sleepSecond = 1;
        Set<Integer> mightExecutionTime = ConcurrentHashMap.newKeySet();
        mightExecutionTime.add(sleepSecond);
        mightExecutionTime.add(sleepSecond << 1);
        Runnable testLockRunnable = () -> {
            LockInfo lockInfo = new LockInfo("test");
            Instant preLock = Instant.now();
            lock.lock(lockInfo);
            // 断言获取到了
            assert lock.holdLock(lockInfo.getResource(), lockInfo.getToken());
            try {
                Thread.sleep(sleepSecond * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lock.unlock(lockInfo);
            Instant afterLock = Instant.now();
            long seconds = Duration.between(preLock, afterLock).toSeconds();
            assert mightExecutionTime.remove((int) seconds);
        };
        new Thread(testLockRunnable).start();
        new Thread(testLockRunnable).start();
        Thread.sleep(sleepSecond * 1000 + 1000);
        System.out.println("ok");
    }

}
