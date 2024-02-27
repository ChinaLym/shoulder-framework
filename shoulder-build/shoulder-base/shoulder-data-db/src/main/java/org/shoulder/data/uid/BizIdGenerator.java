package org.shoulder.data.uid;

import org.shoulder.data.mybatis.template.entity.BizEntity;

/**
 * bizId 生成算法
 *
 * @author lym
 */
public interface BizIdGenerator {

    /**
     * 生成 bizId
     */
    String generateBizId(BizEntity entity, Class<? extends BizEntity> entityClass);

}
