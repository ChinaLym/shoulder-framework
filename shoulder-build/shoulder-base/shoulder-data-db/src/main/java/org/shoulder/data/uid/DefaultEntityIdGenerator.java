package org.shoulder.data.uid;

import org.apache.ibatis.reflection.MetaObject;
import org.shoulder.core.guid.LongGuidGenerator;
import org.shoulder.core.guid.StringGuidGenerator;

/**
 * 默认的实体 id 生成器
 *
 * @author lym
 */
public class DefaultEntityIdGenerator implements EntityIdGenerator {

    private final LongGuidGenerator longGuidGenerator;

    private final StringGuidGenerator stringGuidGenerator;

    public DefaultEntityIdGenerator(LongGuidGenerator longGuidGenerator, StringGuidGenerator stringGuidGenerator) {
        this.longGuidGenerator = longGuidGenerator;
        this.stringGuidGenerator = stringGuidGenerator;
    }

    @Override
    public Object genId(MetaObject metaObject, Class<?> idType) {
        if (String.class.isAssignableFrom(idType)) {
            return stringGuidGenerator.nextId();
        }
        if (idType == long.class || idType == Long.class) {
            return longGuidGenerator.nextId();
        }
        throw new IllegalStateException("The DefaultEntityIdGenerator only support long/String id!");
    }
}
