package org.shoulder.data.uid;

import org.apache.ibatis.reflection.MetaObject;
import org.shoulder.data.sequence.SequenceGenerator;

import java.util.Date;

/**
 * bizId 生成算法 —— 序号表
 *
 * @author lym
 */
public class SequenceEntityIdGenerator implements EntityIdGenerator {

    private final SequenceGenerator sequenceGenerator;

    public SequenceEntityIdGenerator(SequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    private String parseDataVersion(MetaObject metaObject, Class<?> actuallyClass) {
        return "0";
    }

    private String parseSequenceName(MetaObject metaObject, Class<?> actuallyClass) {
        return actuallyClass.getSimpleName();
    }

    @Override public Object genId(MetaObject metaObject, Class<?> idType) {
        Class<?> actuallyClass = metaObject.getOriginalObject().getClass();
        String sequenceName = parseSequenceName(metaObject, actuallyClass);
        long sequence = sequenceGenerator.next(sequenceName);
        if (String.class.isAssignableFrom(idType)) {
            String dataVersion = parseDataVersion(metaObject, actuallyClass);
            return IdGenerationUtil.generateId(new Date(), dataVersion, sequence);
        }
        if (idType == long.class || idType == Long.class) {
            return sequence;
        }
        throw new IllegalStateException("The DefaultEntityIdGenerator only support long/String id!");
    }
}
