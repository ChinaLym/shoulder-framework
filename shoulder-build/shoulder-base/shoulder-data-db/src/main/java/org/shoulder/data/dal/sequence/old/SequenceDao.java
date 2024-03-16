package org.shoulder.data.dal.sequence.old;

import org.shoulder.data.dal.sequence.exceptions.SequenceException;

/**
 * 序列DAO接口
 *
 *
 * @author lym
 *
 */
public interface SequenceDao {
    /**
     * 取得下一个可用的序列区间
     *
     * @param name 序列名称
     * @return 返回下一个可用的序列区间
     * @throws SequenceException
     */
    SequenceRange nextRange(String name) throws SequenceException;

}
