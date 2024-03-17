package org.shoulder.data.dal.sequence.dao;

import org.shoulder.data.dal.sequence.model.SequenceRange;

import java.util.List;

/**
 * sequence DAO 通用方法
 *
 * @author lym
 */
public interface SequenceDao {

    void initialize() throws Exception;

    /**
     * 根据 sequenceName 拿一段 sequence
     */
    SequenceRange getNextSequence(String sequenceName) throws Exception;


    SequenceRange loadNextSequenceFromDbViaNewTransaction(final String sequenceSourceId, final SequenceRange localSequenceRange);

    /**
     * Monitor：Retry times this DAO try to update sequence until success
     */
    int getMaxRetryTimes();
}
