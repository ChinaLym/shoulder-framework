package org.shoulder.data.dal.sequence;

import org.shoulder.data.dal.sequence.exceptions.SequenceException;

/**
 *
 * @author
 *
 */
public interface IShardingFailoverSequence {

    public static enum MODE {
        MASTER, FAILOVER
    }

    /**
     *
     *
     * @param mode
     * @return
     * @throws SequenceException
     */
    long nextValue(MODE mode) throws SequenceException;
}
