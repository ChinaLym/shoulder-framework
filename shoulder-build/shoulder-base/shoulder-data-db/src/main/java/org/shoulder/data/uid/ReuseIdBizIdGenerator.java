package org.shoulder.data.uid;

import org.shoulder.data.mybatis.template.entity.BizEntity;

/**
 * bizId 生成算法 —— return id
 *
 * @author lym
 */
public class ReuseIdBizIdGenerator implements ConditionalBizIdGenerator {

    @Override public boolean support(BizEntity entity, Class<? extends BizEntity> entityClass) {
        return entity.getId() != null;
    }

    @Override
    public String generateBizId(BizEntity entity, Class<? extends BizEntity> entityClass) {
        return String.valueOf(entity.getId());
    }
}
