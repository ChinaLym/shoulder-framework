package org.shoulder.core.lock;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.util.IpUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * 通用锁信息定义
 *
 * @author lym
 */
public class LockInfo {

    /**
     * 锁定的资源，可通过该值解析为什么锁，哪里锁
     */
    private String resource;

    /**
     * 持有者，可通过该值解析持有应用 / 机器 / 线程 等
     */
    private String owner;

    /**
     * 令牌，用于操作锁（获取、解锁、修改）在达到 ttl 之前，必须通过该令牌，才能对锁进行操作
     */
    private String token;

    /**
     * 加锁时间，加锁时间
     */
    private Instant lockTime = Instant.now();

    /**
     * 什么时候到期，可用于阻塞时间依据或
     */
    private Instant releaseTime;

    /**
     * 重入次数，用于可重入锁
     */
    private int reenterCount = 0;

    /**
     * 版本号，用于乐观锁
     */
    private int version = 0;

    public LockInfo() {
    }

    public LockInfo(String resource, Duration lockLife) {
        this.resource = resource;
        // appId:instanceId:threadId 不用于区分是否唯一，token 需要唯一
        this.owner = IpUtils.getIp() + ":" + AppInfo.appId() + ":" + Thread.currentThread().getId();
        this.token = UUID.randomUUID().toString();
        this.lockTime = Instant.now();
        this.releaseTime = lockTime.plus(lockLife);
    }

    /**
     * 默认锁 1 分钟
     */
    public LockInfo(String lockId) {
        this(lockId, Duration.ofMinutes(1));
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Instant releaseTime) {
        this.releaseTime = releaseTime;
    }

    public int getReenterCount() {
        return reenterCount;
    }

    public void setReenterCount(int reenterCount) {
        this.reenterCount = reenterCount;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Instant getLockTime() {
        return lockTime;
    }

    public void setLockTime(Instant lockTime) {
        this.lockTime = lockTime;
    }

    @Override
    public String toString() {
        return "LockInfo{" +
            "resource='" + resource + '\'' +
            ", owner='" + owner + '\'' +
            ", token='" + token + '\'' +
            ", ttl=" + releaseTime +
            ", reenterCount=" + reenterCount +
            ", version=" + version +
            ", createTime=" + lockTime +
            '}';
    }
}
