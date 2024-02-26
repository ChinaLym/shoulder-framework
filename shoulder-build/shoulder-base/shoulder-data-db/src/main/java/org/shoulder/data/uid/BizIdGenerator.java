package org.shoulder.data.uid;

import org.shoulder.data.mybatis.template.entity.BizEntity;

/**
 * bizId 生成算法
 *
 * @author lym
 */
public interface BizIdGenerator {

    /**
     * 是否支持为该类生成
     */
    boolean support(BizEntity entity, Class<? extends BizEntity> entityClass);

    /**
     * 生成 bizId
     */
    String generateBizId(BizEntity entity, Class<? extends BizEntity> entityClass);

}
