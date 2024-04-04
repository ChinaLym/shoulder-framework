package org.shoulder.data.sequence.generator.rule;

import org.shoulder.data.sequence.model.SequenceRange;
import org.shoulder.data.sequence.model.SequenceResult;
import org.shoulder.data.sequence.exceptions.SequenceRuleValidateException;

import java.util.Date;

/**
 * @author lym
 */
public interface ISequenceCombinationRule {

    String SYSTEM_DATE = "systemDate";

    String DB_ID = "dbId";

    String TABLE_ID = "tableId";

    String SEQUENCE_VALUE = "sequenceValue";

    /**
     * @return
     */
    String[] getComponentsOrder();

    /**
     * @return
     */
    String getCombinationRule();

    /**
     * @return
     */
    String getRuleName();

    /**
     * @param combinatedElements
     * @return
     */
    SequenceResult applyCombinationRule(SequenceRange sequenceRange, long value,
                                        Object... combinatedElements);

    SequenceResult applyCombinationRule(Date systemDate, String elasticId, String groupId,
                                        String tableId, long value, Object... combinatedElements);

    /**
     * @throws SequenceRuleValidateException
     */
    void validateRule() throws SequenceRuleValidateException;
}
