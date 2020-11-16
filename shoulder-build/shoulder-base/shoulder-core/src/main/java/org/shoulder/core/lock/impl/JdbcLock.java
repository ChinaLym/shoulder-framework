package org.shoulder.core.lock.impl;

import org.shoulder.core.lock.LockInfo;
import org.shoulder.core.lock.ServerLock;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;

/**
 * 锁持久层
 * 数据库锁，解锁时应置为过期，而非删除记录
 * 注意频繁增删元素，可能对索引结构造成一定影响
 *
 * @author lym
 */
public class JdbcLock implements ServerLock {

    private static final Logger log = LoggerFactory.getLogger(JdbcLock.class);


    private static final String INSERT_FIELDS = "resource, owner, token, lock_time, release_time";

    /**
     * 查询锁信息
     */
    private static final String BASE_QUERY_STATEMENT =
        "select owner, token, version, lock_time, release_time, version from server_lock where resource=";

    /**
     * 悲观 创建锁- 插入
     */
    private static final String CREATE_LOCK_STATEMENT = "insert into server_lock (" + INSERT_FIELDS
        + ") values (?, ?, ?, ?, ?) where not exists (select resource from server_lock where resource=?";

    /**
     * 悲观 释放锁- 删除记录
     */
    private static final String RELEASE_LOCK_DELETE_STATEMENT =
        "delete server_lock where resource=? and token=?";


    /**
     * 清理已经过期的锁
     */
    private static final String CLEAN_LOCK_STATEMENT = "delete server_lock where release_time < now()";

    /**
     * 尝试加锁
     *
     * @deprecated 不使用更新，容易因为事务隔离机制导致意外结果
     */
    private static final String UPDATE_LOCK_STATEMENT = "update server_lock set " +
        "owner=? and token=? and  version=? and  lock_time=? and release_time=? "
        + "where resource=? and release_time < ?";


    /**
     * jdbc 模板
     */
    private JdbcTemplate jdbc;

    /**
     * 数据库 - DTO 转换
     */
    private RowMapper<LockInfo> rowMapper = getRowMapper();

    /**
     * 最小阻塞时间
     */
    private Duration minBlockTime = Duration.ofMillis(10);

    @Override
    @Nullable
    public LockInfo getLockInfo(String resource) {
        LockInfo lockInfo = jdbc.queryForObject(BASE_QUERY_STATEMENT, rowMapper, resource);
        if (lockInfo != null) {
            lockInfo.setResource(resource);
        }
        return lockInfo;
    }

    @Override
    public boolean tryLock(LockInfo lockInfo, Duration maxBlockTime) throws InterruptedException {
        while (!tryLock(lockInfo)) {
            // 阻塞，直至加锁成功
            Thread.sleep(minBlockTime.toMillis());
        }
        return true;
    }

    @Override
    public boolean tryLock(LockInfo lockInfo) {
        return jdbc.update(CREATE_LOCK_STATEMENT,
            lockInfo.getResource(), lockInfo.getOwner(), lockInfo.getToken(),
            lockInfo.getLockTime(), lockInfo.getReleaseTime(), lockInfo.getResource()) != 0;
    }

    @Override
    public boolean holdLock(String resource, String token) {
        return StringUtils.equals(token,
            jdbc.queryForObject("select token from server_lock where resource=?", String.class, resource));
    }

    @Override
    public void unlock(String resource, String token) {
        jdbc.update(RELEASE_LOCK_DELETE_STATEMENT, resource, token);
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
            entity.setLockTime(rs.getTime(3).toInstant());
            entity.setReleaseTime(rs.getTime(4).toInstant());
            entity.setVersion(rs.getInt(5));
            return entity;
        }
    }

}
