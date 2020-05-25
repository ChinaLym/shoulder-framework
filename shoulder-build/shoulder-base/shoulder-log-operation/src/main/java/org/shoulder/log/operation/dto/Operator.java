package org.shoulder.log.operation.dto;

import org.shoulder.log.operation.constants.OpLogConstants;

/**
 * 操作者信息
 * 主要用于兼容不同框架，其中不带 default 的方法返回值不得为空
 * @author lym
 */
public interface Operator {

    /** 操作者 用户标识 【必填】 */
    String getUserId();

    /** 操作者所在机器 ip */
    String getIp();

    /** 操作者 用户姓名/昵称 */
    default String getUserName(){
        return null;
    }

    /** 操作者的 mac地址 */
    default String getMac(){
        return null;
    }

    /** 操作者 实名关联的人id */
    default String getPersonId(){
        return null;
    }

    /** 操作者所属 组织标识 */
    default String getUserOrgId(){
        return null;
    }

    /** 操作者 登录终端类型 */
    default String getTerminalType(){
        return OpLogConstants.TerminalType.SYSTEM;
    }

}
