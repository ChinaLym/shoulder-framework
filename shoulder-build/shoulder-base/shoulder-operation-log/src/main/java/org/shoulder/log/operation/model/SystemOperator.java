package org.shoulder.log.operation.model;

import org.shoulder.core.context.AppInfo;
import org.shoulder.core.util.AddressUtils;
import org.shoulder.log.operation.enums.TerminalType;

import javax.annotation.Nonnull;

/**
 * 有些操作（如定时任务）是非用户操作的，那么操作者就是系统了
 *
 * @author lym
 */
public class SystemOperator implements Operator {

    // 系统用户各个字段不允许修改

    private final String systemUserId;
    private final String hostAddress;
    private final String mac;

    /**
     * 构造器
     *
     * @param systemUserId 服务标识
     * @param hostAddress  本机地址
     * @param mac          本机 MAC 地址
     */
    private SystemOperator(String systemUserId, String hostAddress, String mac) {
        this.systemUserId = systemUserId;
        this.hostAddress = hostAddress;
        this.mac = mac;
    }

    private static class SingletonHolder {
        private static final SystemOperator INSTANCE =
            new SystemOperator("system." + AppInfo.appId(), AddressUtils.getIp(), AddressUtils.getMac());
    }

    public static SystemOperator getInstance() {
        return SingletonHolder.INSTANCE;
    }

    @Nonnull
    @Override
    public String getUserId() {
        return systemUserId;
    }

    @Override
    public String getRemoteAddress() {
        return hostAddress;
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
