package org.shoulder.log.operation.dto;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.util.IpUtils;
import org.shoulder.log.operation.enums.TerminalType;

import javax.annotation.Nonnull;

/**
 * 有些操作（如定时任务）是非用户操作的，那么操作者就是系统了
 *
 * @author lym
 */
public class SystemOperator implements Operator {

    // 系统用户各个字段不允许修改

    private final String userId;
    private final String ip;
    private final String mac;

    private SystemOperator(String userId, String ip, String mac) {
        this.userId = userId;
        this.ip = ip;
        this.mac = mac;
    }

    private static class SingletonHolder {
        private static final SystemOperator INSTANCE =
            new SystemOperator("system." + AppInfo.appId(), IpUtils.getIp(), IpUtils.getMac());
    }

    public static SystemOperator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Nonnull
    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public String getUserName() {
        return "_system_";
    }

    @Nonnull
    @Override
    public TerminalType getTerminalType() {
        return TerminalType.SYSTEM;
    }

    @Override
    public String getTerminalId() {
        return mac;
    }

}
