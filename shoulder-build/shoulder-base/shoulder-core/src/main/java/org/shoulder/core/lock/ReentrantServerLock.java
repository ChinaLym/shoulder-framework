package org.shoulder.core.lock;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 通过装饰者模式实现重入次数计数
 *
 * 因为每次重入时，只需判断是否持有锁即可，无需更新中间件中的值，减少网络通信与写开销
 *
 * @author lym
 */
public class ReentrantServerLock implements ServerLock {

    private ServerLock delegate;

    /**
     * 当前进程持有的锁的重入计数器
     * 锁标识 - 重入次数
     */
    private ConcurrentMap<String, Integer> reentrantCount = new ConcurrentHashMap<>();

    @Override
    public boolean tryLock(Duration lockLife) {
        /*if(delegate.tryLock()){

        }*/
        return false;
    }

    @Override
    public boolean tryLock(Duration maxWait, Duration lockLife) throws InterruptedException {
        return false;
    }

    @Override
    public boolean tryLock(String toLockResource, String valueEx, Duration holdDuration) {
        return false;
    }

    @Override
    public boolean holdLock(String toLockResource, String valueEx) {
        return false;
    }

    @Override
    public void release(String toLockResource, String valueEx) {

    }
}
