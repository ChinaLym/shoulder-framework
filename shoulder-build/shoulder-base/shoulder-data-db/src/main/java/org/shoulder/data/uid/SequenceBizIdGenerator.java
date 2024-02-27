package org.shoulder.data.uid;

import org.shoulder.data.mybatis.template.entity.BizEntity;

/**
 * bizId 生成算法 —— 序号表
 *
 * @author lym
 */
public class SequenceBizIdGenerator implements ConditionalBizIdGenerator {

    @Override public boolean support(BizEntity entity, Class<? extends BizEntity> entityClass) {
        return false;
    }

    @Override
    public String generateBizId(BizEntity entity, Class<? extends BizEntity> entityClass) {
        // todo 通过部分字段信息 + bizType + SequenceGenerator
        throw new RuntimeException();
    }
}
