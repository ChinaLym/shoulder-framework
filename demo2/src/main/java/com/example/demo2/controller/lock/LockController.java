package com.example.demo2.controller.lock;

import com.example.demo2.entity.UserEntity;
import com.example.demo2.service.IUserService;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.lock.impl.JdbcLock;
import org.shoulder.data.mybatis.base.controller.BaseController;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 使用 mybatis-plus，基本不需要写基础代码
 *
 * @author lym
 */
@SkipResponseWrap // 跳过包装方便演示
@RestController
@RequestMapping("lock")
public class LockController extends BaseController<IUserService, UserEntity> {


    private ServerLock lock;

    public LockController(DataSource dataSource) {
        lock = new JdbcLock(dataSource);
    }


    private volatile String shareKey = "abc";

    private volatile String operationToken = "xxx";


    /**
     * http://localhost:8080/lock/tryLock
     */
    @RequestMapping("tryLock")
    public boolean tryLock() {
        LockInfo lockInfo = new LockInfo(shareKey);
        if(lock.tryLock(lockInfo)){
            operationToken = lockInfo.getToken();
            return true;
        }
        return false;
    }

    /**
     * http://localhost:8080/lock/tryLockMax5s
     */
    @RequestMapping("tryLockMax5s")
    public boolean tryLockMax5s() throws InterruptedException {
        LockInfo lockInfo = new LockInfo(shareKey);
        if(lock.tryLock(lockInfo, Duration.ofSeconds(5))){
            operationToken = lockInfo.getToken();
            return true;
        }
        return false;
    }

    /**
     * http://localhost:8080/lock/lock
     */
    @RequestMapping("lock")
    public boolean lock() {
        LockInfo lockInfo = new LockInfo(shareKey);
        lock.lock(lockInfo);
        operationToken = lockInfo.getToken();
        return true;
    }

    /**
     * http://localhost:8080/lock/holdLock?useShare=true   模拟持锁线程查看是否持锁
     * http://localhost:8080/lock/holdLock?useShare=false  模拟新的线程查看是否持锁
     */
    @RequestMapping("holdLock")
    public boolean holdLock(Boolean useShare) {
        LockInfo shareLock = new LockInfo(shareKey);
        if(Boolean.TRUE.equals(useShare)){
            shareLock.setToken(operationToken);
        }
        return lock.holdLock(shareLock);
    }

    /**
     * http://localhost:8080/lock/unlock?release=true
     * http://localhost:8080/lock/unlock?release=false
     */
    @RequestMapping("unlock")
    public String unlock(Boolean release) {
        LockInfo shareLock = new LockInfo(shareKey);
        if(Boolean.TRUE.equals(release)){
            shareLock.setToken(operationToken);
        }
        lock.unlock(shareLock);
        return "ok";
    }



    // ------------------- jdk interface --------------------


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
     * http://localhost:8080/lock/jdk/tryLockMax5s
     */
    @RequestMapping("jdk/tryLockMax5s")
    public boolean tryLockMax5s_jdk() throws InterruptedException {
        if(lock.tryLock(5, TimeUnit.SECONDS)){
            return true;
        }
        return false;
    }

    /**
     * 可重入测试
     * http://localhost:8080/lock/jdk/unlock
     */
    @RequestMapping("jdk/unlock")
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
