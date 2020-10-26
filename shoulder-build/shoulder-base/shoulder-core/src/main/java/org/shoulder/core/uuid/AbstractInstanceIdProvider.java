package org.shoulder.core.uuid;


/**
 * 通用实现，允许程序在运行时，
 *
 * @author lym
 */
public abstract class AbstractInstanceIdProvider implements InstanceIdProvider {

    /**
     * 非法标识，常用于标记加载失败
     */
    private static final long ILLEGAL = -1;

    /**
     * 当前标识
     */
    protected volatile long instanceId = ILLEGAL;

    @Override
    public long getCurrentInstanceId() {
        if (instanceId < 0) {
            synchronized (this) {
                if (instanceId < 0) {
                    instanceId = loadInstanceId();
                    if (instanceId == ILLEGAL) {
                        throw new IllegalStateException();
                    }
                }
            }
        }
        return instanceId;
    }

    protected long loadInstanceId() {
        return -1;
    }

}
