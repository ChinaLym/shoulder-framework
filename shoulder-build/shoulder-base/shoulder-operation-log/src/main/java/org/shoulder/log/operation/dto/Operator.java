package org.shoulder.log.operation.dto;

import org.shoulder.log.operation.enums.TerminalType;

/**
 * 操作者信息
 * 主要用于兼容不同框架，其中不带 default 的方法返回值不得为空
 *
 * @author lym
 */
public interface Operator {

    /**
     * 操作者 用户标识 【必填】
     */
    String getUserId();

    /**
     * 操作者 用户姓名/昵称
     */
    default String getUserName() {
        return null;
    }

    /**
     * 操作者 实名关联的人id
     */
    default String getPersonId() {
        return null;
    }

    /**
     * 操作者所属组标识
     */
    default String getUserOrgId() {
        return null;
    }

    /**
     * 操作者所属组名称
     */
    default String getUserOrgName() {
        return null;
    }

    /**
     * 操作者 登录终端类型
     */
    default TerminalType getTerminalType() {
        return TerminalType.SYSTEM;
    }

    /**
     * 操作者所在机器 ip
     */
    default String getIp() {
        return null;
    }

    /**
     * 操作者的 terminalId
     */
    default String getTerminalId() {
        return null;
    }

    /**
     * 操作者的 terminalInfo
     */
    default String getTerminalInfo() {
        return null;
    }

}
