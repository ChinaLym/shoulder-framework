package org.shoulder.core.guid;


/**
 * 获取实例标识接口，用于 guid 中填充实例标识
 * 实现该接口即可修改获取方式，如固定、从配置文件中读取、从 redis、zookeeper、etcd 等
 * 如果设计 instanceId 回放策略，一般是从队列头部取，在尾部放入
 *
 * @author lym
 */
public interface InstanceIdProvider {

    /**
     * 获取本应用/服务进程的 instanceId
     *
     * @return guid 通常为较小正整数
     */
    long getCurrentInstanceId();

}
