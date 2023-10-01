package com.example.demo2.controller.lock;

import com.example.demo2.entity.UserEntity;
import com.example.demo2.service.IUserService;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.lock.impl.JdbcLock;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.shoulder.web.template.crud.BaseControllerImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

/**
 * 锁的使用介绍
 * 注意：
 * 1. 每个接口仅演示单个方法，测完需要注意记得 unlock ！！！！！
 * 2. 锁是非常重要的，不仅仅要直到如何使用，还要知道锁的原理是什么才不容易出 bug
 *      shoulder 框架提供的锁，锁的持有者标志：资源id + 线程id + appId + appInstanceId；
 *
 * @author lym
 */
@SkipResponseWrap // 跳过包装方便演示
@RestController
@RequestMapping("lock")
public class LockController extends BaseControllerImpl<IUserService, UserEntity> {

    private ServerLock lock;

    public LockController(DataSource dataSource) {
        lock = new JdbcLock(dataSource);
    }


    private volatile String shareKey = "abc";

    private volatile String globalOperationToken = "xxx";


    /**
     * http://localhost:8080/lock/tryLock
     * @return 是否获取成功
     */
    @RequestMapping("tryLock")
    public boolean tryLock() {
        LockInfo lockInfo = new LockInfo(shareKey);
        if (lock.tryLock(lockInfo)) {
            globalOperationToken = lockInfo.getToken();
            return true;
        }
        return false;
    }

    /**
     * 尝试获取锁，最多等 5s，获取到后永久锁定
     * 5秒 http://localhost:8080/lock/tryLockMax5s?holdTime=pt5s
     * 10天 http://localhost:8080/lock/tryLockMax5s?holdTime=p10d
     */
    @RequestMapping("tryLockMax5s")
    public boolean tryLockMax5s(String holdTime) throws InterruptedException {
        LockInfo lockInfo = new LockInfo(shareKey, Duration.parse(holdTime));
        if (lock.tryLock(lockInfo, Duration.ofSeconds(5))) {
            globalOperationToken = lockInfo.getToken();
            return true;
        }
        return false;
    }


    /**
     * http://localhost:8080/lock/lock
     * 如果可以上锁，会立马返回 true
     * 否则将一直等待，直到有人 unlock
     */
    @RequestMapping("lock")
    public boolean lock() {
        LockInfo lockInfo = new LockInfo(shareKey);
        lock.lock(lockInfo);
        globalOperationToken = lockInfo.getToken();
        return true;
    }

    /**
     * http://localhost:8080/lock/holdLock?global=true   模拟持锁线程查看是否持锁
     * http://localhost:8080/lock/holdLock?global=false  模拟新的线程查看是否持锁
     *
     * @param global 是否用全局的共享锁
     */
    @RequestMapping("holdLock")
    public boolean holdLock(Boolean global) {
        LockInfo shareLock = new LockInfo(shareKey);
        if (Boolean.TRUE.equals(global)) {
            shareLock.setToken(globalOperationToken);
        }
        return lock.holdLock(shareLock);
    }

    /**
     * http://localhost:8080/lock/unlock?global=true
     * http://localhost:8080/lock/unlock?global=false
     */
    @RequestMapping("unlock")
    public String unlock(Boolean global) {
        LockInfo shareLock = new LockInfo(shareKey);
        if (Boolean.TRUE.equals(global)) {
            shareLock.setToken(globalOperationToken);
        }
        lock.unlock(shareLock);
        return "ok";
    }


    // ======================================================================================
    // ===============================【 jdk interface 】=====================================
    // ======================================================================================

    /**
     * http://localhost:8080/lock/jdk/lock
     */
    @RequestMapping("jdk/lock")
    public boolean lock_jdk() {
        lock.lock();
        try {
            // 这10s内其他请求无法获取锁
            Thread.sleep(10 * 1000);
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * http://localhost:8080/lock/jdk/tryLock
     * 可以连续访问该接口，10s内只有一次可以
     */
    @RequestMapping("jdk/tryLock")
    public boolean jdkTryLock() {
        boolean locked = lock.tryLock();
        try {
            // 这10s内其他请求无法获取锁
            Thread.sleep(10 * 1000);
        } catch (InterruptedException ignored) {
        } finally {
            lock.unlock();
        }
        return locked;
    }


    /**
     * 尝试加锁，最大等待时间为 5s，5s拿不到，则返回false
     * http://localhost:8080/lock/jdk/tryLockMax5s
     */
    @RequestMapping("jdk/tryLockMax5s")
    public boolean tryLockMax5s_jdk() throws InterruptedException {
        if (lock.tryLock(5, TimeUnit.SECONDS)) {
            return true;
        }
        return false;
    }

    @RequestMapping("jdk/unlock")
    public String jdk_unlock() throws InterruptedException {
        lock.unlock();
        return "ok";
    }
    /**
     * 可重入测试，测试前请确保已经将锁释放：
     *    释放测试前面 api 时候加的锁：http://localhost:8080/lock/jdk/unlock
     *
     * 测试：http://localhost:8080/lock/jdk/reentrant
     */
    @RequestMapping("jdk/reentrant")
    public String reentrant_jdk() throws InterruptedException {
        lock.lock();
        try {
            lock.lock();
            try {
                System.out.println("reentrant_jdk test");
            } finally {
                lock.unlock();
            }
        } finally {
            lock.unlock();
        }
        return "ok";
    }


}
