package org.shoulder.log.operation.dto;

/**
 * 操作者信息
 * 主要用于兼容不同框架，其中不带 default 的方法返回值不得为空
 * @author lym
 */
public interface Operator {

    /** 操作者 用户标识 【必填】 */
    String getUserId();

    /** 操作者 ip */
    String getIp();

    /** 操作者 用户姓名 */
    default String getUserName(){
        return null;
    }

    /** 操作的mac地址 */
    default String getMac(){
        return null;
    }

    /** 操作者实名关联的人员id */
    default String getPersonId(){
        return null;
    }

}
