package org.shoulder.data.dal.sequence.old;

import org.shoulder.data.dal.sequence.exceptions.SequenceException;

/**
 * 序列接口
 *
 *
 * @author lym
 *
 */
public interface Sequence {
    /**
     * 取得序列下一个值
     *
     * @return 返回序列下一个值
     * @throws SequenceException
     */
    long nextValue() throws SequenceException;
}
