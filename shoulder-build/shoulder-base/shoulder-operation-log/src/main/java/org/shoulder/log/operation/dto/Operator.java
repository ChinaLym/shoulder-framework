package org.shoulder.log.operation.dto;

import org.shoulder.log.operation.enums.TerminalType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 操作者信息
 * 主要用于兼容不同框架，其中不带 default 的方法返回值不得为空
 *
 * @author lym
 */
public interface Operator {

    /**
     * 操作者 【非空】
     *
     * @return 用户标识
     */
    @NonNull
    String getUserId();

    /**
     * 操作者 用户姓名/昵称
     *
     * @return 用户姓名
     */
    @Nullable
    default String getUserName() {
        return null;
    }

    /**
     * 操作者 实名关联的人id
     *
     * @return 实名标识
     */
    @Nullable
    default String getPersonId() {
        return null;
    }

    /**
     * 操作者所属组标识
     *
     * @return 用户组标识
     */
    @Nullable
    default String getUserOrgId() {
        return null;
    }

    /**
     * 操作者所属组名称
     *
     * @return 用户组名称
     */
    @Nullable
    default String getUserOrgName() {
        return null;
    }

    /**
     * 操作者 登录终端类型
     *
     * @return 登录终端类型 【非空】
     */
    @NonNull
    default TerminalType getTerminalType() {
        return TerminalType.SYSTEM;
    }

    /**
     * 操作者所在机器 ip
     *
     * @return 处理机器/服务/docker 标识
     */
    @Nullable
    default String getIp() {
        return null;
    }

    /**
     * 操作者的 terminalId
     *
     * @return 操作者会话标识，用于追踪
     */
    @Nullable
    default String getTerminalId() {
        return null;
    }

    /**
     * 操作者的 terminalInfo
     *
     * @return 登录终端信息，如系统名称、系统信息、浏览器信息、手机信息
     */
    @Nullable
    default String getTerminalInfo() {
        return null;
    }

}
