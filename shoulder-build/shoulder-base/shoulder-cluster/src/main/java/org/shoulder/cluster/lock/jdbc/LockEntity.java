package org.shoulder.cluster.lock.jdbc;

import java.time.Instant;

/**
 * entity
 *
 * @author lym
 */
public class LockEntity {


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
     * 什么时候到期，可用于阻塞时间依据或
     */
    private Instant ttl;

    /**
     * 重入次数，用于可重入锁
     */
    private int reenterCount;

    /**
     * 版本号，用于乐观锁
     */
    private int version;

    /**
     * 创建时间，加锁时间
     */
    private Instant createTime;

}
