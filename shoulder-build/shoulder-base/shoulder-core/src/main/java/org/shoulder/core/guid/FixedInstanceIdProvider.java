package org.shoulder.core.guid;

/**
 * 新建时候就固定，如：从配置中获取
 *
 * @author lym
 */
public class FixedInstanceIdProvider implements InstanceIdProvider {

    private final long instanceId;

    public FixedInstanceIdProvider(long instanceId) {
        this.instanceId = instanceId;
    }

    @Override
    public long getCurrentInstanceId() {
        return instanceId;
    }
}
