package org.shoulder.data.dal.sequence.generator;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.log.LoggerFactory;

import org.shoulder.data.dal.sequence.model.SequenceRange;
import org.shoulder.data.dal.sequence.dao.SequenceDao;
import org.shoulder.data.dal.sequence.exceptions.CombinationSequenceException;
import org.shoulder.data.dal.sequence.model.SequenceResult;
import org.shoulder.data.dal.sequence.generator.rule.ISequenceCombinationRule;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;

import java.util.*;


/**
 * 序列生成器，生成 x 位序列
 * 获取数据库当前时间
 *
 * @author lym
 */
public class CombinationSequenceGenerator implements SequenceGenerator {

    protected final Logger logger = LoggerFactory.getLogger("MONITOR");
    // todo put DEFAULT

    @Getter
    @Setter
    private Map<String, ISequenceCombinationRule> sequenceRuleMap;

    @Getter
    @Setter
    private SequenceDao sequenceDao;

    @Override
    public SequenceResult getNextValue(final String sequenceName, final String ruleName)
        throws DataAccessException {

        ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(ruleName);

        long value;
        int retryTimes = sequenceDao.getMaxRetryTimes();
        Exception ex = null;
        while (retryTimes > 0) {
            try {
                // 1. 获取 range
                SequenceRange sequenceRange = getCombinateSequenceRange(sequenceName);
                // 2. 获取合格的具体 value
                value = sequenceRange.genNextValue();
                if (value >= sequenceRange.getValue() + sequenceRange.getStep() || value > sequenceRange.getMax()) {
                    retryTimes--;
                    continue;
                }
                // 3. 根据规则生成 result
                SequenceResult result = combinationRule.applyCombinationRule(sequenceRange, value);
                return result;
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                ex = e;
            }
            retryTimes = 0;
        }

        throw new CombinationSequenceException(
            "Failed to find combination sequence range(" + sequenceName + ") with retry(" + sequenceDao.getMaxRetryTimes()
                + "), or meet exception", ex);

    }

    private SequenceRange getCombinateSequenceRange(String sequenceName) {
        try {
            return sequenceDao.getNextSequence(sequenceName);
        } catch (Exception e) {
            String msg = "Failed to find sequenceRange: " + e.getMessage();
            logger.error(msg, e);
            throw new CombinationSequenceException(msg, e);
        }
    }

}
