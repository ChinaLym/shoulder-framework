package org.shoulder.core.lock;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.util.AddressUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * 通用锁信息定义
 * 注意分布式系统中，时钟不可靠，除非使用一台机器的时间戳
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
    private LocalDateTime lockTime;

    /**
     * 什么时候到期，可用于阻塞时间依据或
     */
    private LocalDateTime releaseTime;

    /**
     * 申请持锁时间
     * 该值为持锁时间期望值，实际代码持锁时间必然会小于该值
     * 实现上，尽量不包含申请锁、等待加锁过程消耗的时间，加锁成功响应时间，尽量让 holdTime 贴近实际持锁时间
     */
    private Duration holdTime;

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

    public LockInfo(String resource, Duration holdTime) {
        this(resource,
            // appId:instanceId:threadId 不用于区分是否唯一，token 需要唯一
            AddressUtils.getIp() + AppInfo.cacheKeySplit()
                    + AppInfo.appId() + AppInfo.cacheKeySplit()
                    + AppInfo.instanceId() + AppInfo.cacheKeySplit()
                    + Thread.currentThread().getId(),
            holdTime
        );
    }

    public LockInfo(String resource, String owner, Duration holdTime) {
        this(resource,
            owner,
            UUID.randomUUID().toString(),
            LocalDateTime.now(),
            LocalDateTime.now().plus(holdTime),
            holdTime,
            0, 0
        );
    }

    public LockInfo(String resource, String owner, String token, LocalDateTime lockTime, LocalDateTime releaseTime, Duration holdTime, int reenterCount, int version) {
        this.resource = resource;
        this.owner = owner;
        this.token = token;
        this.lockTime = lockTime;
        this.releaseTime = releaseTime;
        this.holdTime = holdTime;
        this.reenterCount = reenterCount;
        this.version = version;
    }

    /**
     * 默认锁 1 年
     */
    public LockInfo(String lockId) {
        this(lockId, ChronoUnit.YEARS.getDuration());
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

    public LocalDateTime getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(LocalDateTime releaseTime) {
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

    public LocalDateTime getLockTime() {
        return lockTime;
    }

    public void setLockTime(LocalDateTime lockTime) {
        this.lockTime = lockTime;
    }

    public Duration getHoldTime() {
        return holdTime;
    }

    public void setHoldTime(Duration holdTime) {
        this.holdTime = holdTime;
    }

    @Override
    public String toString() {
        return "LockInfo{" +
            "resource='" + resource + '\'' +
            ", owner='" + owner + '\'' +
            ", token='" + token + '\'' +
            ", lockTime=" + lockTime +
            ", releaseTime=" + releaseTime +
            ", holdTime=" + holdTime +
            ", reenterCount=" + reenterCount +
            ", version=" + version +
            '}';
    }
}
