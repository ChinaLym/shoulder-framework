package org.shoulder.data.sequence;

import org.shoulder.data.sequence.model.SequenceResult;
import org.springframework.dao.DataAccessException;

import java.util.List;

/**
 * 直接适用该类生成序列
 *
 * @author lym
 */
public interface SequenceGenerator {

    long next(String sequenceName) throws DataAccessException;

    List<Long> next(String sequenceName, int size) throws DataAccessException;
}
