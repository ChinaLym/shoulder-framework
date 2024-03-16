package org.shoulder.data.dal.sequence.generator;

import org.shoulder.data.dal.sequence.model.SequenceResult;
import org.springframework.dao.DataAccessException;

import java.util.List;

/**
 * 直接适用该类生成序列
 *
 * @author lym
 */
public interface SequenceGenerator {

    SequenceResult getNextValue(String sequenceName, String ruleName,
                                List<Object> shardingParameters) throws DataAccessException;

    SequenceResult getNextValue(String sequenceName, String ruleName) throws DataAccessException;

    SequenceResult getNextValueWithRealDate(String sequenceName, String ruleName,
                                            List<Object> shardingParameters)
        throws DataAccessException;

    SequenceResult getNextValueWithRealDate(String sequenceName, String ruleName)
        throws DataAccessException;

    SequenceResult prepareSequenceValue(String sequenceName, String ruleName,
                                        List<Object> shardingParameters) throws DataAccessException;

    SequenceResult prepareSequenceValue(String sequenceName, String ruleName)
        throws DataAccessException;

    SequenceResult prepareSequenceValue(String ruleName, String ruleString, String uid,
                                        List<Object> shardingParameters) throws DataAccessException;

    SequenceResult prepareSequenceValue(String ruleName, String ruleString, String uid)
        throws DataAccessException;

}
