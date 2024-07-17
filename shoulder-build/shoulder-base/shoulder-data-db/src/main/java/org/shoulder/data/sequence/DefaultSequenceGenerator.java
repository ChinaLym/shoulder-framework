package org.shoulder.data.sequence;

import lombok.Getter;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.data.log.ShoulderDBLoggers;
import org.shoulder.data.sequence.dao.SequenceDao;
import org.shoulder.data.sequence.exceptions.CombinationSequenceException;
import org.shoulder.data.sequence.model.SequenceRange;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.List;


/**
 * 序列生成器，生成 x 位序列
 * 获取数据库当前时间
 *
 * @author lym
 */
public class DefaultSequenceGenerator implements SequenceGenerator {

    protected final Logger logger = ShoulderDBLoggers.SEQUENCE;
    // todo put DEFAULT

    // @Getter
    // @Setter
    // private Map<String, ISequenceCombinationRule> sequenceRuleMap;

    @Getter
    private final SequenceDao sequenceDao;

    public DefaultSequenceGenerator(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    @Override
    public long next(String sequenceName) throws DataAccessException {
        // ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(ruleName);

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
                // SequenceResult result = combinationRule.applyCombinationRule(sequenceRange, value);
                return value;
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
                ex = e;
            }
            retryTimes = 0;
        }

        throw new CombinationSequenceException("Failed to find combination sequence range(" + sequenceName + ") with retry(" + sequenceDao.getMaxRetryTimes() + "), or meet exception", ex);

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

    @Override
    public List<Long> next(String sequenceName, int size) throws DataAccessException {
        AssertUtils.isTrue(size > 0 && size < 1000, CommonErrorCodeEnum.ILLEGAL_PARAM, "size must in [1, 1000]");
        SequenceRange sequenceRange = getCombinateSequenceRange(sequenceName);
        // 2. 获取合格的具体 value
        List<Long> batchResult = sequenceRange.genNextValues(size);
        if (batchResult != null) {
            return batchResult;
        }
        batchResult = new ArrayList<>(size);
        int batchThreathold = 10;
        for (int left = size; left > 0; ) {
            if (left > batchThreathold) {
                batchResult.addAll(next(sequenceName, batchThreathold));
                left -= batchThreathold;
            }
            batchResult.add(next(sequenceName));
            left--;
        }
        return batchResult;
    }
}
