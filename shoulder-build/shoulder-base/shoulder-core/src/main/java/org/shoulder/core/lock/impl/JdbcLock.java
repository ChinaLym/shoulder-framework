package org.shoulder.core.lock.impl;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.lock.AbstractServerLock;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

/**
 * 锁持久层
 * 数据库锁，解锁时应置为过期，而非删除记录
 * 注意频繁增删元素，可能对索引结构造成一定影响
 * todo
 *
 * @author lym
 */
public class JdbcLock extends AbstractServerLock implements ServerLock {

    private static final Logger log = LoggerFactory.getLogger(JdbcLock.class);


    private static final String INSERT_FIELDS = "resource, owner, token, version, lock_time, release_time";

    /**
     * 查询锁信息
     */
    private static final String BASE_QUERY_STATEMENT =
        "SELECT owner, token, version, lock_time, release_time FROM system_lock WHERE resource=?";

    /**
     * 悲观 创建锁- 插入
     */
    private static final String CREATE_LOCK_STATEMENT =
        "INSERT INTO system_lock (" + INSERT_FIELDS + ") " +
            "VALUES (?, ?, ?, 0, now(), ?)";

    /**
     * 悲观 创建锁- 插入
     */
    private static final String CREATE_IF_UNEXISTS_LOCK_STATEMENT =
        "INSERT INTO system_lock (" + INSERT_FIELDS + ") " +
            "SELECT ?, ?, ?, 0, ?, ? FROM DUAL " +
            "WHERE NOT EXISTS(SELECT resource FROM system_lock WHERE resource = ?)";


    /**
     * 悲观 释放锁- 删除记录
     */
    private static final String RELEASE_LOCK_DELETE_STATEMENT =
        "DELETE FROM system_lock WHERE resource=? AND token=?";


    /**
     * 清理已经过期的锁
     */
    private static final String CLEAN_LOCK_STATEMENT = "DELETE FROM system_lock WHERE release_time < NOW()";

    /**
     * 尝试加锁
     *
     * @deprecated 不使用更新，容易因为事务隔离机制导致意外结果，且不同数据库支持情况不同
     */
    private static final String UPDATE_LOCK_STATEMENT =
        "INSERT INTO system_lock (" + INSERT_FIELDS + ") VALUES (?, ?, ?, 0, ?, ?) " +
            "ON DUPLICATE KEY UPDATE owner=?, token=?, version=0, lock_time=now(), release_time=? where release_time < now()";
    private static final String UPDATE_LOCK_BASE_ON_VERSION_STATEMENT =
        "INSERT INTO system_lock (" + INSERT_FIELDS + ") VALUES (?, ?, ?, 0, ?, ?) " +
            "ON DUPLICATE KEY UPDATE release_time < now()";


    /**
     * jdbc 模板
     */
    private JdbcTemplate jdbc;

    /**
     * 数据库 - DTO 转换
     */
    private RowMapper<LockInfo> rowMapper = getRowMapper();

    /**
     * 重试等待时间间隔
     * todo 可扩展 / 调整
     */
    private Duration retryBlockTime = Duration.ofMillis(20);

    public JdbcLock(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    public JdbcLock(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * 使用的 REQUIRES_NEW 的原因：在出现嵌套逻辑和嵌套事务的情况下，可能会出现脏读问题, 举例:
     * <p>
     * 使用者开启事务，拿到 sqlSessionA
     * 读取某个资源的所信息，存入一级缓存（lockInfoA{resource='xxx', token='a'})
     * <p>
     * 调用加锁，触发新的事务，拿到 sqlSessionB，写入加锁信息（lockInfoB{resource='xxx', token='b'}）
     * 提交事务，关闭 关闭sqlSessionB
     * <p>
     * 此时再次读取这个资源的锁信息，因为本次读取和前一次是同一个 sqlSessionA，默认走一级缓存，会脏读
     * 从缓存中拿到旧的信息（lockInfoA{resource='xxx', token='a'}）
     * <p>
     * 一级缓存是与 sqlSession 关联的，因此每次 read 也 new 一个事务，从而构建一个新的 sqlSession，即可以解决这个问题。
     *
     * @param resource 资源
     * @return 资源对应的锁信息
     */
    @Override
    @Nullable
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = BaseRuntimeException.class)
    public LockInfo getLockInfo(String resource) {
        LockInfo lockInfo = jdbc.queryForObject(BASE_QUERY_STATEMENT, rowMapper, resource);
        if (lockInfo != null) {
            lockInfo.setResource(resource);
        }
        return lockInfo;
    }

    /**
     * 【阻塞时间精确性注意】因为每次尝试都与数据库交互，故也是耗时的，尤其是数据库繁忙时，举例：
     * 使用者期望最多阻塞5s，拿不到锁立即返回，结果阻塞5s加上轮询数据库耗时（不确定将阻塞多久，比如5s），导致总供阻塞了10s
     * 这是使用者意料之外的，故需要统计该时间
     * <p>
     * 框架实现-最多阻塞 expectMaxBlockTime + 一次尝试获取时间
     *
     * @param lockInfo           锁信息
     * @param exceptMaxBlockTime 等待获取锁最大阻塞时间，实际必然大于该值，框架实现尽量贴近该值
     * @return 是否获取成功
     * @throws InterruptedException 阻塞时被其他线程打断
     */
    @Override
    public boolean tryLock(LockInfo lockInfo, Duration exceptMaxBlockTime) throws InterruptedException {
        // 返回截止时间
        Instant startTime = Instant.now();
        Instant deadline = startTime.plus(exceptMaxBlockTime);
        for (int tryTimes = 0; !tryLock(lockInfo); tryTimes++) {
            // 剩余最长可等待时间
            Duration maxBlockTime = Duration.between(Instant.now(), deadline);
            if (maxBlockTime.isNegative() || maxBlockTime.isZero()) {
                // 剩余可等待时间 <= 0：达到最大时间且没有获取到
                log.info("try lock FAIL with {}! {}", exceptMaxBlockTime, lockInfo);
                return false;
            }
            // 取较小的
            Duration blockTime = retryBlockTime.compareTo(maxBlockTime) < 0 ? retryBlockTime : maxBlockTime;
            log.trace("try lock {} for {} times.", lockInfo.getResource(), tryTimes);
            // 阻塞，直至加锁成功
            Thread.sleep(blockTime.toMillis());
        }
        log.debug("try lock SUCCESS cost {}! {}", Duration.between(startTime, Instant.now()), lockInfo);
        return true;
    }

    /**
     * 使用 REQUIRES_NEW 的原因：存在事务时加锁无效，其他线程无法感知已经加锁。举例：
     * - 使用者开启事务，调用加锁方法，未提交事务，则默认情况下加锁也不提交
     * - 其他线程读取数据库锁信息发现未加锁，也尝试加锁，导致同时执行
     *
     * 使用REQUIRES_NEW可以保证，调用加锁方法后，其他线程读取最新锁信息的可见性
     *
     * @param lockInfo 锁信息
     * @return 是否加锁成功
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = BaseRuntimeException.class)
    public boolean tryLock(LockInfo lockInfo) {
        boolean locked = false;
        try {
            locked = jdbc.update(
                CREATE_LOCK_STATEMENT,
                lockInfo.getResource(), lockInfo.getOwner(), lockInfo.getToken(), lockInfo.getReleaseTime()) != 0;
        } catch (DataAccessException ignored) {
            // DuplicateKeyException | DeadlockLoserDataAccessException
            // 已经存在：其他线程已经获取到锁
        }

/* // insert on unExists
        boolean locked = jdbc.update(
            CREATE_LOCK_STATEMENT,
            lockInfo.getResource(), lockInfo.getOwner(), lockInfo.getToken(),
            lockInfo.getLockTime(), lockInfo.getReleaseTime(), lockInfo.getResource()
        ) != 0;
*/
        log.debug("try lock {}! {}", locked ? "SUCCESS" : "FAIL", lockInfo);
        return locked;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = BaseRuntimeException.class)
    public boolean holdLock(String resource, String token) {
        return jdbc.queryForObject("select count(resource) from system_lock where resource=? and token=?",
            Integer.class, resource, token) > 0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = BaseRuntimeException.class)
    public void unlock(String resource, String token) {
        jdbc.update(RELEASE_LOCK_DELETE_STATEMENT, resource, token);
        log.debug("unlock {} with token={}", resource, token);
    }

    protected RowMapper<LockInfo> getRowMapper() {
        return new LockInfoRowMapper();
    }


    private static class LockInfoRowMapper implements RowMapper<LockInfo> {

        @Override
        public LockInfo mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            // owner, token, version, lock_time, release_time
            LockInfo entity = new LockInfo();
            entity.setOwner(rs.getString(0));
            entity.setToken(rs.getString(1));
            entity.setVersion(rs.getInt(2));
            entity.setLockTime(rs.getTimestamp(3).toLocalDateTime());
            entity.setReleaseTime(rs.getTimestamp(4).toLocalDateTime());
            return entity;
        }
    }

}
