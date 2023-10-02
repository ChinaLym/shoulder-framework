package org.shoulder.core.lock.impl;

import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.lock.AbstractDistributeLock;
import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * 基于 数据库 的分布式锁
 * 可以根据自身使用情况合理调整，举例：频繁增删元素，可能对索引结构造成一定影响，若锁的key比较固定，则推荐解锁时采用更新而非删除
 * 注意存在事务 / 嵌套事务的影响，数据交互耗时对阻塞最大时长的影响
 *
 * @author lym
 */
public class JdbcLock extends AbstractDistributeLock implements ServerLock {

    private static final String INSERT_FIELDS = "resource, owner, token, version, lock_time, release_time";

    /**
     * 查询锁信息
     */
    private static final String BASE_QUERY_STATEMENT =
        "SELECT owner, token, version, lock_time, release_time FROM system_lock WHERE resource=?";

    /**
     * 悲观 创建锁- 插入
     */
    private static final String INSERT_STATEMENT =
        "INSERT INTO system_lock (" + INSERT_FIELDS + ") " +
            "VALUES (?, ?, ?, 0, now(), ?)";

    /**
     * 悲观 创建锁- 插入
     */
    private static final String INSERT_IF_NOT_EXISTS_STATEMENT =
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
            // insert on unExists
            locked = jdbc.update(
                INSERT_IF_NOT_EXISTS_STATEMENT,
                lockInfo.getResource(), lockInfo.getOwner(), lockInfo.getToken(),
                lockInfo.getLockTime(), lockInfo.getReleaseTime(), lockInfo.getResource()
            ) != 0;
            /*locked = jdbc.update(
                INSERT_STATEMENT,
                lockInfo.getResource(), lockInfo.getOwner(), lockInfo.getToken(), lockInfo.getReleaseTime()) != 0;*/
        } catch (DataAccessException ignored) {
            // DuplicateKeyException | DeadlockLoserDataAccessException
            // 已经存在：其他线程已经获取到锁
        }
        log.trace("try lock {}! {}", locked ? "SUCCESS" : "FAIL", lockInfo);
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
    public void unlock(String resource, @Nonnull String token) {
        if(StringUtils.isEmpty(token)) {
            // 参数问题
            AssertUtils.notEmpty(token, CommonErrorCodeEnum.ILLEGAL_PARAM, "unlock token can't be empty");
        }
        int changedLine = jdbc.update(RELEASE_LOCK_DELETE_STATEMENT, resource, token);
        log.debug("unlock {} with token={}", resource, token);
        if(changedLine == 0) {
            // 有可能是使用者代码写错：
            // 1. token 传错；
            // 2. resource 传错，当前未持锁；
            // 3. 被其他线程误释放
            // 4. （非代码bug）本节点执行时间过长，锁已经过期被清理释放了
            // 这些都不是预期内的报错--抛异常
            throw new BaseRuntimeException(CommonErrorCodeEnum.UNKNOWN.getCode(), "unlock " + resource + " but do nothing! with token=" + token);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = BaseRuntimeException.class)
    public boolean cleanExpiredLock() {
        return jdbc.update(CLEAN_LOCK_STATEMENT) > 0;
    }

    protected RowMapper<LockInfo> getRowMapper() {
        return new LockInfoRowMapper();
    }


    private static class LockInfoRowMapper implements RowMapper<LockInfo> {

        @Override
        public LockInfo mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            // owner, token, version, lock_time, release_time
            LockInfo entity = new LockInfo();
            entity.setOwner(rs.getString(1));
            entity.setToken(rs.getString(2));
            entity.setVersion(rs.getInt(3));
            entity.setLockTime(rs.getTimestamp(4).toLocalDateTime());
            entity.setReleaseTime(rs.getTimestamp(5).toLocalDateTime());
            return entity;
        }
    }

}
