package org.shoulder.data.uid;

import org.shoulder.data.mybatis.template.entity.BizEntity;
import org.shoulder.data.sequence.SequenceGenerator;

import java.util.Date;

/**
 * bizId 生成算法 —— 序号表
 *
 * @author lym
 */
public class SequenceBizIdGenerator implements ConditionalBizIdGenerator {

    private final SequenceGenerator sequenceGenerator;

    public SequenceBizIdGenerator(SequenceGenerator sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override public boolean support(BizEntity entity, Class<? extends BizEntity> entityClass) {
        return true;
    }

    @Override
    public String generateBizId(BizEntity entity, Class<? extends BizEntity> entityClass) {
        // todo 通过部分字段信息 + bizType + SequenceGenerator
        String sequenceName = parseSequenceName(entity, entityClass);
        String dataVersion = parseDataVersion(entity, entityClass);
        long sequence = sequenceGenerator.next(sequenceName);
        return IdGenerationUtil.generateId(new Date(), "0", sequence);
    }

    private String parseDataVersion(BizEntity entity, Class<? extends BizEntity> entityClass) {
        return "0";
    }

    private String parseSequenceName(BizEntity entity, Class<? extends BizEntity> entityClass) {
        return entityClass.getSimpleName();
    }
}
