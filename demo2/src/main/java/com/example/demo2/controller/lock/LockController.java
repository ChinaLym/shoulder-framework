package com.example.demo2.controller.lock;

import com.example.demo2.entity.UserEntity;
import com.example.demo2.service.IUserService;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ReentrantServerLock;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.lock.ServerLockAcquireProxy;
import org.shoulder.core.lock.impl.JdbcLock;
import org.shoulder.core.lock.impl.JdkLock;
import org.shoulder.core.util.AssertUtils;
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

    // 锁很多时候是锁某个资源 key，demo方便用 abc 举例，demo 为了方便，每次启动要锁的资源不同
    private volatile String shareKey = UUID.randomUUID().toString();

    // 加锁/解锁的钥匙，demo 为了方便，全局统一用一个，且每次重启不变
    private volatile String globalOperationToken = "testToken";

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
     * 尝试获取 DB 锁，持有时间=HLOD_LOCK_DURATION（5min）
     * http://localhost:8080/lock/tryLock
     * @return 是否获取成功
     */
    @RequestMapping("tryLock")
    public String tryLock() {
        // new LockInfo 不填会自动设置随机 UUID token，简化了使用
        LockInfo lockInfo = new LockInfo(shareKey, HLOD_LOCK_DURATION);
        boolean locked = jdbcLock.tryLock(lockInfo);
        String tip = "但锁已被使用，【未拿到】";
        if(locked) {
            // 暂存token，方便后续unlock使用
            globalOperationToken = lockInfo.getToken();
            tip = "【拿到】，将于 5min 后自动释放"; //HLOD_LOCK_DURATION
        }
        return "尝试拿JDBC锁-" + tip;
    }

    /**
     * 尝试获取 DB 锁，如果获取时暂时获取不到最多等5s(5s内不断自动重试)，持有时间=HLOD_LOCK_DURATION（5min）
     * 5秒 http://localhost:8080/lock/tryLockWithWait?holdTime=pt5s
     * 100s http://localhost:8080/lock/tryLockWithWait?holdTime=pt100s
     */
    @RequestMapping("tryLockWithWait")
    public String tryLock(String holdTime) throws InterruptedException {
        LockInfo lockInfo = new LockInfo(shareKey, Duration.parse(holdTime));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        boolean locked = jdbcLock.tryLock(lockInfo, Duration.ofSeconds(5));
        stopWatch.stop();

        String tip = "但锁已被使用，5s内尝试多次均【未拿到】";
        if (locked) {
            globalOperationToken = lockInfo.getToken();
            tip = "耗时 " + stopWatch.getTotalTimeMillis() + ",ms 【拿到】，将于 " + Duration.parse(holdTime).toSeconds() + "s 后自动释放"; //HLOD_LOCK_DURATION
        }
        return "尝试在5s内拿JDBC锁-" + tip;
    }


    /**
     * http://localhost:8080/lock/lock
     * 如果可以上锁，会立马返回 true
     * 否则将一直等待，直到有人 unlock
     */
    @RequestMapping("lock")
    public String lock() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LockInfo lockInfo = new LockInfo(shareKey, HLOD_LOCK_DURATION);
        jdbcLock.lock(lockInfo);
        stopWatch.stop();

        globalOperationToken = lockInfo.getToken();
        return "阻塞拿锁，【拿到】耗时：" + stopWatch.getTotalTimeSeconds() + "s";
    }

    /**
     * http://localhost:8080/lock/holdLock?global=true   模拟持锁线程查看是否持锁（持有 token）
     * http://localhost:8080/lock/holdLock?global=false  模拟非持锁线程查看是否持锁（没有 token）
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
     * http://localhost:8080/lock/unlock?global=true 持有锁会释放
     * http://localhost:8080/lock/unlock?global=false 未持有锁（token不对）无法释放，抛异常
     */
    @RequestMapping("unlock")
    public String unlock(Boolean global) {
        LockInfo shareLock = new LockInfo(shareKey);
        if (Boolean.TRUE.equals(global)) {
            shareLock.setToken(globalOperationToken);
        }
        jdbcLock.unlock(shareLock);
        return "released success！";
    }


    /**
     * 可重入测试：http://localhost:8080/lock/jdbc/reentrant
     *
     * 不正经的可重入：持锁线程可以再次直接获取锁，不管获取多少次，释放一次就释放
     *
     * JDK可重入定义：持锁线程可以再次直接获取锁，且释放锁的次数需要与获取锁的次数相同才认为是释放锁 【Shoulder采用】
     */
    @RequestMapping("/jdbc/reentrant")
    public String reentrant_jdbc() throws InterruptedException {
        LockInfo shareLock = new LockInfo(shareKey, HLOD_LOCK_DURATION);
        ReentrantServerLock jdbcLockx = new ReentrantServerLock(jdbcLock);

        // 第一次加锁
        jdbcLockx.lock(shareLock);
        System.out.println("reentrant_JDBC test 第一次获取锁成功！");
        try {
            // 其他人这时不可以获取锁
            boolean othersCanNotLock = jdbcLockx.tryLock(new LockInfo(shareKey));
            AssertUtils.isFalse(othersCanNotLock, CommonErrorCodeEnum.CODING);

            // 第二次加锁
            jdbcLockx.lock(shareLock);
            System.out.println("reentrant_JDBC test 第二次获取锁成功！");
            try {
                System.out.println("reentrant_JDBC test OK！");
            } finally {
                jdbcLockx.unlock(shareLock);
                System.out.println("reentrant_JDBC test 第一次释放锁成功！");
            }

            // 尽管持锁者释放过一次了，但由于未全部释放，其他人这时不可以获取锁
            othersCanNotLock = jdbcLockx.tryLock(new LockInfo(shareKey));
            AssertUtils.isFalse(othersCanNotLock, CommonErrorCodeEnum.CODING);

        } finally {
            jdbcLockx.unlock(shareLock);
            System.out.println("reentrant_JDBC test 第二次释放锁成功！【全部释放～现在其他人可以获取该锁了】");
        }
        return "JDBC锁-可重入测试 OK";
    }

    /**
     * demo 的以上很多方法都没有自动调用unlock，为了防止关闭前忘记调用unlock导致锁住5分钟，影响重启后调试，才有这段代码，实际不需要这么写
     */
    @PreDestroy
    public void autoCleanForDemo() {
        try {
            unlock(true);
        } catch (Exception ignored){}
        try {
            unlock(false);
        } catch (Exception ignored){}
    }
}
