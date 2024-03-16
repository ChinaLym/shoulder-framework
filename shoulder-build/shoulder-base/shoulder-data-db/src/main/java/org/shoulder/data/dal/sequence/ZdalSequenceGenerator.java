package org.shoulder.data.dal.sequence;

import lombok.Getter;
import lombok.Setter;
import org.shoulder.data.dal.sequence.dao.JdbcSequenceDAO;
import org.shoulder.data.dal.sequence.generator.CombinationSequenceGenerator;
import org.shoulder.data.dal.sequence.dao.DefaultCacheableSequenceDAO;
import org.shoulder.data.dal.sequence.generator.rule.ISequenceCombinationRule;
import org.shoulder.data.dal.sequence.generator.rule.RealTimeSequenceCombinationRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @deprecated just use config
 * @author lym
 */
public class ZdalSequenceGenerator extends CombinationSequenceGenerator<JdbcSequenceDAO> {

    private final static String DEFAULT_RULE_KEY = "DEFAULT";
    private final static String DEFAULT_RULE_VALUE = "{length(sequenceValue)=8}";
    /**
     * Simple rule configuration
     */
    @Getter @Setter
    private Map<String, String> rules;
    private String tableName;
    @Getter @Setter
    private XDataSource zdalDataSource;
    @Getter
    private List<String> shardingColumns = new ArrayList<>();

    /**
     * -1: use default value (1)
     */
    @Getter @Setter
    private int retryTimes = -1;
    /**
     * -1: use default value (1)
     */
    @Getter @Setter
    private long minValue = -1;
    /**
     * -1: use default value (99999999)
     */
    @Getter @Setter
    private long maxValue = -1;
    /**
     * -1: use default value (1000)
     */
    @Getter @Setter
    private long step = -1;


    public void init() throws Exception {
        /*
         * Init sequence dao according dataSource & shardingColumns & tableName
         */
        DefaultCacheableSequenceDAO combinationSequenceDAO = new DefaultCacheableSequenceDAO();
        combinationSequenceDAO.setDataSource(dataSource);
        combinationSequenceDAO.setSequenceShardingColumnNamesArray(shardingColumns);
        combinationSequenceDAO.setSequenceTableName(tableName);

        if (retryTimes > 0) {
            combinationSequenceDAO.setRetryTimes(retryTimes);
        }
        if (minValue > 0) {
            combinationSequenceDAO.setMinValue(minValue);
        }
        if (maxValue > 0) {
            combinationSequenceDAO.setMaxValue(maxValue);
        }
        if (step > 0) {
            combinationSequenceDAO.setStep(step);
        }
        combinationSequenceDAO.setCacheSize(cacheSize);
        combinationSequenceDAO.setCacheExpireSeconds(cacheExpireSeconds);
        combinationSequenceDAO.initialize();

        this.setSequenceDao(combinationSequenceDAO);

        if (super.getSequenceRuleMap() == null) {
            super.setSequenceRuleMap(new HashMap<String, ISequenceCombinationRule>());
        }

        /*
         * Init sequence rule according rules string map
         */
        if (getRules() != null) {
            for (Map.Entry<String, String> entry : getRules().entrySet()) {
                super.getSequenceRuleMap().put(entry.getKey(),
                    generateRule(entry.getKey(), entry.getValue()));
            }
        }

        /*
         * Put default sequence rule to sequenceRuleMap
         */
        if (this.getSequenceRuleMap().get(DEFAULT_RULE_KEY) == null) {
            super.getSequenceRuleMap().put(DEFAULT_RULE_KEY,
                generateRule(DEFAULT_RULE_KEY, DEFAULT_RULE_VALUE));
        }

    }

    private ISequenceCombinationRule generateRule(String ruleName, String rule) throws Exception {
        RealTimeSequenceCombinationRule sequenceCombinationRule = new RealTimeSequenceCombinationRule();
        sequenceCombinationRule.setRuleName(ruleName);
        sequenceCombinationRule.setCombinationRule(rule);
        sequenceCombinationRule.initialize();
        return sequenceCombinationRule;
    }

}
