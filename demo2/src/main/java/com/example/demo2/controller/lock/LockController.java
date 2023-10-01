package com.example.demo2.controller.lock;

import com.example.demo2.entity.UserEntity;
import com.example.demo2.service.IUserService;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ReentrantServerLock;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.lock.ServerLockAcquireProxy;
import org.shoulder.core.lock.impl.JdbcLock;
import org.shoulder.core.lock.impl.JdkLock;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.shoulder.web.template.crud.BaseControllerImpl;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

/**
 * Shoulder 锁的使用介绍
 *
 * 为了让同一套代码应用在单机、集群模式时尽量不改代码, Shoulder允许你仅变更配置，即可适配部署模式！并基于JDK自身接口{@link Lock}提供了一些列实现:
 *    {@link ServerLock}并适配了多种底层模式：JDK ReentrantLock、MemoryLock、JDBC、Redis..
 *    这些实现除了支持各种通用 api ，还考虑了分布式的高性能、高可靠：死锁规避-宕机自动释放、死锁规避-可重入...
 *
 *    Shoulder甚至贴心的为使用者提供了方便自行扩展的代理类： {@link ServerLockAcquireProxy}（快速获得更好的分布式性能）、{@link ReentrantServerLock}(快速获得可重入能力)
 *
 * 目的：让使用者在开发时可以专注业务，而不必同时关注部署模式、底层技术差异！
 *      （因为这些技术的学习完全可以用 shoulder 帮你省下来的时间更专注的学习，高效利用自己的时间和宝贵生命）
 *
 * 注意：
 * 1. 每个接口仅演示单个方法，测完需要注意记得 unlock ！！！！！
 * 2. 锁是非常重要的，不仅仅要直到如何使用，还要知道锁的原理是什么才不容易出 bug
 *      shoulder 框架提供的锁，锁的持有者标志：资源id + 线程id + appId + appInstanceId；
 * 3. 由于这里演示 jdbc lock，故宕机后仍然持锁
 * todo 调整demo结构
 *
 * @author lym
 */
@SkipResponseWrap // 跳过包装方便演示
@RestController
@RequestMapping("lock")
public class LockController extends BaseControllerImpl<IUserService, UserEntity> {

    // 实际一般都是注入，不会用new，这里为了演示方便，所以才用了两个不同字段
    private final ServerLock jdbcLock;
    private final ServerLock jdkLock = new JdkLock();


    public LockController(DataSource dataSource) {
        jdbcLock = new JdbcLock(dataSource);
    }

    // 锁很多时候是锁某个资源 key，demo方便用 abc 举例
    private volatile String shareKey = "abc";

    // 加锁/解锁的钥匙，demo 为了方便，统一用一个
    private volatile String globalOperationToken = UUID.randomUUID().toString();

    private volatile Duration HLOD_LOCK_DURATION = Duration.ofMinutes(5);


    // ======================================================================================
    // ===============================【 jdk interface 】=====================================
    // ======================================================================================

    /**
     * http://localhost:8080/lock/jdk/lockAndHold10s
     */
    @RequestMapping("jdk/lockAndHold10s")
    public String jdk_lockAndHold10s() {
        jdkLock.lock();
        try {
            // 这10s内其他请求无法获取锁
            Thread.sleep(10 * 1000);
        } catch (InterruptedException ignored) {
        } finally {
            // 注意，如果不传入LockInfo，则使用该变量自带的JdkLock，假设当前线程没持有该锁，直接调用会报错哦~~ （同 JDK）
            jdkLock.unlock();
        }
        return "阻塞拿到JDK锁，拿到后持有共计10s后释放了";
    }

    /**
     * http://localhost:8080/lock/jdk/tryLockAndHold10s
     * 可以连续访问该接口，10s内只有一次可以
     */
    @RequestMapping("jdk/tryLockAndHold10s")
    public String jdkTryLock() {
        boolean locked = jdkLock.tryLock();
        try {
            // 这10s内其他请求无法获取锁
            Thread.sleep(10 * 1000);
        } catch (InterruptedException ignored) {
        } finally {
            jdkLock.unlock();
        }
        return  locked ? "尝试拿JDK锁-没拿到，直接返回了" : "尝试拿JDK锁-成功，拿到后持有共计10s后释放了";
    }


    /**
     * 尝试加锁，最大等待时间为 5s，5s拿不到，则返回false，且不自动释放，需要下一个 api释放
     * http://localhost:8080/lock/jdk/tryLockMax5s__notReleaseLock
     */
    @RequestMapping("jdk/tryLockMax5s__notReleaseLock")
    public String tryLockMax5s_jdk() throws InterruptedException {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            boolean locked = jdkLock.tryLock(5, TimeUnit.SECONDS);
            stopWatch.stop();
            String costTip = "用时 " + stopWatch.getTotalTimeMillis() + "ms";
            return  locked ? "尝试拿JDK锁-" + costTip + "-没拿到，直接返回了" : "尝试拿JDK锁" + costTip + "-成功，拿到后直接释放了";
        } finally {
            jdkLock.unlock();
        }
    }

    /**
     * 可重入测试：http://localhost:8080/lock/jdk/reentrant
     */
    @RequestMapping("jdk/reentrant")
    public String reentrant_jdk() throws InterruptedException {
        jdkLock.lock();
        try {
            jdkLock.lock();
            try {
                System.out.println("reentrant_jdk test");
            } finally {
                jdkLock.unlock();
            }
        } finally {
            jdkLock.unlock();
        }
        return "JDK锁-可重入测试 OK";
    }


    // ======================================================================================
    // ===============================【 分布式模式测试（这里用了数据库锁） 】=======================
    // 这些方法用jdkLock也能用，但不做重复演示
    // ======================================================================================


    /**
     * http://localhost:8080/lock/tryLock
     * @return 是否获取成功
     */
    @RequestMapping("tryLock")
    public boolean tryLock() {
        LockInfo lockInfo = new LockInfo(shareKey, HLOD_LOCK_DURATION);
        if (jdbcLock.tryLock(lockInfo)) {
            globalOperationToken = lockInfo.getToken();
            return true;
        }
        return false;
    }

    /**
     * 尝试获取锁，最多等 5s，获取到后永久锁定
     * 5秒 http://localhost:8080/lock/tryLock?holdTime=pt5s
     * 100s http://localhost:8080/lock/tryLock?holdTime=pt100s
     */
    @RequestMapping("tryLock")
    public boolean tryLock(String holdTime) throws InterruptedException {
        LockInfo lockInfo = new LockInfo(shareKey, Duration.parse(holdTime));
        if (jdbcLock.tryLock(lockInfo, Duration.ofSeconds(5))) {
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
        LockInfo lockInfo = new LockInfo(shareKey, HLOD_LOCK_DURATION);
        jdbcLock.lock(lockInfo);
        globalOperationToken = lockInfo.getToken();
        return true;
    }

    /**
     * http://localhost:8080/lock/holdLock?global=true   模拟持锁线程查看是否持锁（持有）
     * http://localhost:8080/lock/holdLock?global=false  模拟新的线程查看是否持锁（通常不持有）
     *
     * @param global 是否用全局的共享锁
     */
    @RequestMapping("holdLock")
    public boolean holdLock(Boolean global) {
        LockInfo shareLock = new LockInfo(shareKey);
        if (Boolean.TRUE.equals(global)) {
            shareLock.setToken(globalOperationToken);
        }
        return jdbcLock.holdLock(shareLock);
    }

    /**
     * http://localhost:8080/lock/unlock?global=true
     * http://localhost:8080/lock/unlock?global=false
     */
    @PreDestroy
    @RequestMapping("unlock")
    public String unlock(Boolean global) {
        LockInfo shareLock = new LockInfo(shareKey);
        if (Boolean.TRUE.equals(global)) {
            shareLock.setToken(globalOperationToken);
        }
        jdbcLock.unlock(shareLock);
        return "ok";
    }

}
