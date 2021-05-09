package org.shoulder.core.guid;


/**
 * 通用实现(保存当且实例标识)，保证一次启动，只调用一次 loadInstanceId
 * 需要子类实现获取实例标识的方法，最好能幂等
 *
 * @author lym
 */
public abstract class AbstractInstanceIdProvider implements InstanceIdProvider {

    /**
     * 非法标识，常用于标记加载失败
     */
    protected static final long ILLEGAL = -1;

    /**
     * 当前实例标识
     */
    protected volatile long instanceId = ILLEGAL;

    @Override
    public long getCurrentInstanceId() {
        if (instanceId < 0) {
            synchronized (this) {
                if (instanceId < 0) {
                    instanceId = assignInstanceId();
                    if (instanceId == ILLEGAL) {
                        throw new IllegalStateException();
                    }
                }
            }
        }
        return instanceId;
    }

    /**
     * 生成一次实例标识(最好幂等)
     *
     * @return 实例号
     */
    protected abstract long assignInstanceId();

    /**
     * 获取进程标识
     * 幂等性
     * @return 进程信息
     */
    /*protected String getProgressInfo(){
        // docker 环境：docker:docker_host:expose_port


        // 非 docker 环境：actual:ip:expose_port （如果多区域部署，ip不能是内网 / 添加部署区域标识）

    }*/

}
