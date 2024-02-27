package org.shoulder.data.uid;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.data.mybatis.template.entity.BizEntity;

import java.util.List;

/**
 * bizId 生成算法 —— 序号表
 *
 * @author lym
 */
public class CompositeBizIdGenerator implements BizIdGenerator {

    private final List<ConditionalBizIdGenerator> bizIdGeneratorList;

    public CompositeBizIdGenerator(List<ConditionalBizIdGenerator> bizIdGeneratorList) {
        this.bizIdGeneratorList = bizIdGeneratorList;
    }


    @Override
    public String generateBizId(BizEntity entity, Class<? extends BizEntity> entityClass) {
        ConditionalBizIdGenerator generator = bizIdGeneratorList.stream().filter(g -> g.support(entity, entityClass)).findFirst().orElse(null);
        AssertUtils.notNull(generator, CommonErrorCodeEnum.CODING, "no match bizGenerator for " + entityClass.getName());

        return generator.generateBizId(entity, entityClass);
    }
}
