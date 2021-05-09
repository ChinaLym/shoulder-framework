package org.shoulder.data.uid;

import org.shoulder.core.guid.AbstractInstanceIdProvider;

/**
 * 基于数据库的实现
 *
 * @author lym
 */
public class DbInstanceIdProvider extends AbstractInstanceIdProvider {
    @Override
    protected long assignInstanceId() {
        return 0;
    }
}
