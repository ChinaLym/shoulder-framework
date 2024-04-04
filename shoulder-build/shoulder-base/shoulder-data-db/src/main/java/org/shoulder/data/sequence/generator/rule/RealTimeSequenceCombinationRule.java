package org.shoulder.data.sequence.generator.rule;

import lombok.Setter;
import org.shoulder.core.util.StringUtils;
import org.shoulder.data.sequence.exceptions.SequenceRuleValidateException;
import org.shoulder.data.sequence.model.SequenceRange;
import org.shoulder.data.sequence.model.SequenceResult;
import org.springframework.beans.factory.InitializingBean;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Example:
 * combination rule: {length(systemDate)=8}{length(dbId)=3}{length(tableId)=3}{length(sequenceValue)=8}
 * @author lym
 *
 */
public class RealTimeSequenceCombinationRule implements ISequenceCombinationRule, InitializingBean {

    public static final String LENGTH                   = "length";

    public static final int    LENGTH_SIZE              = LENGTH.length();

    public static final String LEFT_BRACKETS            = "(";

    public static final String RIGHT_BRACKETS           = ")";

    public static final String LEFT_BRACE               = "{";

    public static final String RIGHT_BRACE              = "}";

    public static final String EQUALS                   = "=";

    public static final String ELEMENT_ELASTIC_DS_INDEX = "eDSID";


    @Setter
    private String             combinationRule;

    @Setter
    private String             ruleName;

    @Setter
    private String[]           componentsOrder;

    private String[]           elementNames;

    private int[]              elementFulfillNumbers;

    /**
     * Use {@link this#initialize}
     * @throws Exception
     */
    @Override
    @Deprecated
    public void afterPropertiesSet() throws Exception {
        if (StringUtils.isEmpty(combinationRule))
            throw new IllegalArgumentException(
                "Validating DefaultSequenceCombinationRule has an error as combinationRule is empty.");
        parserCombinationRule(getCombinationRule());
    }

    protected String parserCombinationRule(String combinationRule) {
        StringBuilder strBuilder = new StringBuilder();
        combinationRule = combinationRule.trim();
        String[] elements = StringUtils.split(combinationRule, "}");
        if (null == elements || elements.length < 1)
            return null;
        int rightBracketIndex = 0, equalsIndex = 0;
        int length = elements.length;
        elementFulfillNumbers = new int[length];
        elementNames = new String[length];
        for (int index = 0; index < length; index++) {
            strBuilder.append("{").append(index).append("}");
            rightBracketIndex = StringUtils.indexOf(elements[index], RIGHT_BRACKETS);
            equalsIndex = StringUtils.indexOf(elements[index], EQUALS);
            elementNames[index] = StringUtils.substring(elements[index], 2 + LENGTH_SIZE,
                rightBracketIndex);
            elementFulfillNumbers[index] = Integer.parseInt(StringUtils.substring(elements[index],
                equalsIndex + 1, elements[index].length()));
        }
        return strBuilder.toString();
    }

    public void initialize() throws Exception {
        this.afterPropertiesSet();
    }

    @Override
    public String[] getComponentsOrder() {
        return componentsOrder;
    }

    @Override
    public String getCombinationRule() {
        return combinationRule;
    }

    @Override
    public String getRuleName() {
        return ruleName;
    }

    @Override
    public SequenceResult applyCombinationRule(SequenceRange sequenceRange, long seqValue,
                                               Object... combinatedElements) {
        if (null == sequenceRange)
            throw new IllegalArgumentException("The passed sequenceRange can not be null.");
        return applyCombinationRule(sequenceRange.getSystemDate(),
            "0", "00",
            "00", seqValue, combinatedElements);
    }

    @Override
    public SequenceResult applyCombinationRule(Date systemDate, String elasticId, String groupId,
                                               String tableId, long seqValue,
                                               Object... combinatedElements) {
        SequenceResult sequenceResult = new SequenceResult();
        StringBuilder strBuilder = new StringBuilder();
        String value = "";
        sequenceResult.setSequenceCreatedDate(systemDate);
        for (int index = 0; index < elementNames.length; index++) {
            if (elementNames[index].equals(SYSTEM_DATE)) {
                if (null != systemDate) {
                    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
                    value = dateFormater.format(systemDate);
                }
            } else if (elementNames[index].equals(DB_ID)) {
                value = groupId;
            }
            if (elementNames[index].equals(TABLE_ID)) {
                value = tableId;
            } else if (elementNames[index].equals(SEQUENCE_VALUE)) {
                value = String.valueOf(seqValue);
            } else if (elementNames[index].equals(ELEMENT_ELASTIC_DS_INDEX)) {
                value = elasticId;
            }
            strBuilder.append(fulfillElement(index, value));
        }
        sequenceResult.setSequenceValue(strBuilder.toString());
        return sequenceResult;
    }

    private Object fulfillElement(int index, Object object) {
        if (index >= elementFulfillNumbers.length)
            return object;
        if (null != object) {
            return StringUtils.alignRight(object.toString(), elementFulfillNumbers[index], "0");
        } else {
            return StringUtils.alignRight("", elementFulfillNumbers[index], "0");
        }
    }

    @Override
    public void validateRule() throws SequenceRuleValidateException {

    }

}
