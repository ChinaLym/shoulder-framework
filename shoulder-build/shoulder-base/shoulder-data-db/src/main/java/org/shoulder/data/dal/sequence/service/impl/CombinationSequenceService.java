package org.shoulder.data.dal.sequence.service.impl;

import org.shoulder.data.dal.sequence.DBSelectorIDRouteCondition;
import org.shoulder.data.dal.sequence.XDataSource;
import org.shoulder.data.dal.sequence.ZdalSequenceService;
import org.shoulder.core.log.LoggerFactory;

import org.shoulder.core.util.StringUtils;
import org.shoulder.data.dal.sequence.model.SequenceRange;
import org.shoulder.data.dal.sequence.service.DBMeshSequenceSQL;
import org.shoulder.data.dal.sequence.service.ICombinationSequenceService;
import org.shoulder.data.dal.sequence.dao.IGenericSequenceDao;
import org.shoulder.data.dal.sequence.exceptions.CombinationSequenceException;
import org.shoulder.data.dal.sequence.model.SequenceResult;
import org.shoulder.data.dal.sequence.rule.ISequenceCombinationRule;
import org.shoulder.data.dal.sequence.util.ZdalJdbcUtil;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.util.CollectionUtils;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;


/**
 *
 * @author lym
 */
public class CombinationSequenceService<T extends IGenericSequenceDao> implements ICombinationSequenceService {

    protected final Logger                                            logger                    = LoggerFactory.getLogger("MONITOR");
    // todo put DEFAULT
    private Map<String, ISequenceCombinationRule>                     sequenceRuleMap;

    private T                                                         sequenceDao;

    private String                                                    combinationSequenceResourceId;

    private List<Object>                                              defaultShardingParameters = new ArrayList<>(1);

    protected List<String>                                            shardingColumns           = new ArrayList<String>();

    private ConcurrentHashMap<String, Map<DBMeshSequenceSQL, String>> realDBMeshSequenceSQL     = new ConcurrentHashMap<String, Map<DBMeshSequenceSQL, String>>();

    private ConcurrentHashMap<String, ReentrantLock>                  realDBMeshSequenceSQLLock = new ConcurrentHashMap<String, ReentrantLock>();

    public String getRealDBMeshSequenceSQL(DBMeshSequenceSQL sequenceSQL, final String sequenceName) {
        Map<DBMeshSequenceSQL, String> realSQLMap = realDBMeshSequenceSQL.get(sequenceName);
        if (realSQLMap == null) {
            if (realDBMeshSequenceSQLLock.get(sequenceName) == null) {
                realDBMeshSequenceSQLLock.putIfAbsent(sequenceName, new ReentrantLock());
            }
            try {
                ReentrantLock lock = realDBMeshSequenceSQLLock.get(sequenceName);
                lock.lock();
                try {
                    if ((realSQLMap = realDBMeshSequenceSQL.get(sequenceName)) == null) {
                        realSQLMap = new HashMap<DBMeshSequenceSQL, String>();
                    }
                    for (DBMeshSequenceSQL dbMeshSequenceSQL : DBMeshSequenceSQL.values()) {
                        realSQLMap.put(dbMeshSequenceSQL,
                            dbMeshSequenceSQL.convertToRealSQL(sequenceName, shardingColumns));
                    }
                    realDBMeshSequenceSQL.putIfAbsent(sequenceName, realSQLMap);
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                throw new CombinationSequenceException(
                    "CombinationSequence Error: Failed to init real DBMesh Sequence SQL sequence name["
                            + sequenceName + "]", e);
            }
        }
        return realSQLMap.get(sequenceSQL);
    }

    public CombinationSequenceService() {
        defaultShardingParameters.add(new Object());
    }

    @Override

    public SequenceResult getNextValue(String sequenceName, String ruleName) {
        return getNextValue(sequenceName, ruleName, defaultShardingParameters);
    }

    @Override

    public SequenceResult getNextValue(final String sequenceName, final String ruleName,
                                       final List<Object> shardingParameters)
                                                                             throws DataAccessException {
        try {
            return AmbushZdalUtil.invokeSequence(new GenericCodeWrapper<SequenceResult>() {
                @Override
                public Object call() throws Exception {
                    if (StringUtils.isEmpty(ruleName) || null == shardingParameters
                        || shardingParameters.isEmpty())
                        return null;
                    boolean useDbMesh = sequenceDao.getDataSource().isUseDbMesh();
                    boolean zdalInitStatusDBMesh = sequenceDao.getDataSource()
                        .getZdalDataSourceConfig().getAttributesConfig().isZdalInitStatusDBMesh();
                    boolean zdal2DBMeshUsingDBMesh = sequenceDao.getDataSource()
                        .getZdalDataSourceConfig().getAttributesConfig().isZdal2DBMeshUsingDBMesh();
                    if (!useDbMesh && !zdal2DBMeshUsingDBMesh) {
                        try {
                            prepareZdalSequenceOperation();
                            ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(
                                ruleName);

                            long value;
                            int retryTimes = sequenceDao.getMaxRetryTimes();
                            Exception ex = null;
                            while (retryTimes > 0) {
                                try {
                                    SequenceRange sequenceRange = findCombinateSequenceRange(
                                        sequenceName, shardingParameters);
                                    value = sequenceRange.genNextValue();
                                    /*
                                     *  Due to we using get and Increment, thus; we need to make sure does it reaches the limit or not
                                     *  Once it reach the limit, need to re-load sequence range from the database.
                                     */
                                    if (value >= sequenceRange.getValue() + sequenceRange.getStep()
                                        || value > sequenceRange.getMax()) {
                                        retryTimes--;
                                        continue;
                                    }
                                    SequenceResult result = applySequenceRule(sequenceRange,
                                        combinationRule, value);
                                    DBType dbType = sequenceDao.getDataSource()
                                        .getDBTypeByLogicalId(sequenceRange.getLogicDBIndex());
                                    result.setDbType(dbType);
                                    return result;
                                } catch (Exception e) {
                                    logger.warn(e.getMessage(), e);
                                    ex = e;
                                }
                                retryTimes = 0;
                            }

                            throw new CombinationSequenceException(
                                "CombinationSequence Error: Failed to find combination sequence range for "
                                        + sequenceName + " in times " + sequenceDao.getMaxRetryTimes()
                                        + ", or meet exception", ex);
                        } finally {
                            finishZdalSequenceOperation();
                        }
                    } else {
                        Exception ex = null;
                        Connection conn = null;
                        PreparedStatement ps = null;
                        ResultSet rs = null;
                        try {
                            prepareDBMeshSequenceOperation(sequenceDao.getDataSource()
                                .isUseDbMesh(), zdalInitStatusDBMesh);
                            prepareDBMeshSequenceRule(useDbMesh, zdal2DBMeshUsingDBMesh,
                                shardingParameters, false);
                            String realSQL = getRealDBMeshSequenceSQL(
                                DBMeshSequenceSQL.SELECT_VALUE_SQL, sequenceName);
                            conn = sequenceDao.getDataSource().getConnection();

                            if (sequenceDao instanceof IBatisSequenceDao) {
                                IBatisSequenceDao iBatisSequenceDao = (IBatisSequenceDao) sequenceDao;
                                String tntId = iBatisSequenceDao.lookupTntId();
                                if (iBatisSequenceDao.isEnableTntId()
                                    && StringUtils.isNotEmpty(tntId)) {
                                    realSQL = "/*ODP: TNT_ID=" + tntId + " */" + realSQL;
                                }
                            }

                            ps = conn.prepareStatement(realSQL);

                            dealWithDBMeshSequenceParameters(shardingColumns, shardingParameters,
                                ps);

                            rs = ps.executeQuery();
                            if (rs.next()) {
                                Date timestamp = rs.getDate("timestamp");
                                String eid = rs.getString("eid");
                                String groupId = rs.getString("groupid");
                                String tableId = rs.getString("tableid");
                                long value = rs.getLong("nextval");
                                ISequenceCombinationRule combinationRule = getSequenceRuleMap()
                                    .get(ruleName);
                                SequenceResult result = combinationRule.applyCombinationRule(
                                    timestamp, eid, groupId, tableId, value);
                                result.setDbType(DBType.DBMESH);
                                return result;
                            }

                        } catch (Exception e) {
                            logger.warn(e.getMessage(), e);
                            ex = e;
                        } finally {
                            finishDBMeshSequenceOperation();
                            ZdalJdbcUtil.close(rs, ps, conn);
                        }

                        throw new CombinationSequenceException(
                            "CombinationSequence Error: Failed to find combination sequence range for "
                                    + sequenceName + " in times " + sequenceDao.getMaxRetryTimes()
                                    + ", or meet exception", ex);
                    }
                }
            }, "getNextValue", sequenceName, ruleName, shardingParameters);
        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }
            logger.error("Generate sequence error", e);
            return null;
        }

    }

    @Override

    public SequenceResult getNextValueWithRealDate(String sequenceName, String ruleName)
                                                                                        throws DataAccessException {
        return getNextValueWithRealDate(sequenceName, ruleName, defaultShardingParameters);
    }

    @Override

    public SequenceResult getNextValueWithRealDate(String sequenceName, String ruleName,
                                                   List<Object> shardingParameters)
                                                                                   throws DataAccessException {
        return getNextValue(sequenceName, ruleName, shardingParameters);
    }


    private SequenceRange findCombinateSequenceRange(String sequenceName,
                                                     List<Object> shardingParameters)
                                                                                                throws IntegrityViolationException {
        try {
            SequenceContext.enterSequenceDaoFlag();
            return sequenceDao.getNextSequence(sequenceName, shardingParameters);
        } catch (Exception e) {
            String msg = "Failed to find sequenceRange: " + e.getMessage();
            logger.error(msg, e);
            throw new CombinationSequenceException(msg, e);
        } finally {
            SequenceContext.clearSequenceDaoFlag();
        }
    }

    public Map<String, ISequenceCombinationRule> getSequenceRuleMap() {
        return sequenceRuleMap;
    }

    @Deprecated
    public void setSequenceRuleMap(Map<String, ISequenceCombinationRule> sequenceRuleMap) {
        this.sequenceRuleMap = sequenceRuleMap;
    }

    public T getSequenceDao() {
        return sequenceDao;
    }

    @Deprecated
    public void setSequenceDao(T sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    @Override

    public SequenceResult prepareSequenceValue(String sequenceName, String ruleName)
                                                                                    throws DataAccessException {
        return prepareSequenceValue(sequenceName, ruleName, defaultShardingParameters);
    }

    @Override

    public SequenceResult prepareSequenceValue(final String sequenceName, final String ruleName,
                                               final List<Object> shardingParameters)
                                                                                     throws DataAccessException {
        try {
            return AmbushZdalUtil.invokeSequence(new GenericCodeWrapper<SequenceResult>() {
                @Override
                public SequenceResult call() throws Exception {
                    if (StringUtils.isEmpty(ruleName) || null == shardingParameters
                        || shardingParameters.isEmpty())
                        return null;
                    SequenceResult result;
                    boolean useDbMesh = sequenceDao.getDataSource().isUseDbMesh();
                    boolean zdalInitStatusDBMesh = sequenceDao.getDataSource()
                        .getZdalDataSourceConfig().getAttributesConfig().isZdalInitStatusDBMesh();
                    boolean zdal2DBMeshUsingDBMesh = sequenceDao.getDataSource()
                        .getZdalDataSourceConfig().getAttributesConfig().isZdal2DBMeshUsingDBMesh();
                    if (!useDbMesh && !zdal2DBMeshUsingDBMesh) {
                        boolean isElastic = ZdalZoneUtil.isElastic();

                        ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(
                            ruleName);
                        try {
                            prepareZdalSequenceOperation();
                            RuleAppliedResult appliedResult = sequenceDao
                                .applySequenceTableRule(shardingParameters);
                            if (isElastic) {
                                try {
                                    result = prepareSequenceValueWithElastic(ruleName,
                                        appliedResult);
                                } catch (Exception e) {
                                    // in Exception , will directly return
                                    throw new CombinationSequenceException(
                                        "CombinationSequence Error in elasticMode: "
                                                + e.getMessage(), e);
                                }
                                if (result != null)
                                    return result;
                                // when the result is null,
                                // indicates the uid is an elastic uid,
                                // but it's not in an elastic zone, aka: in it's normal zone
                            } else {
                                // only not elastic uid will try self adjust mode
                                result = prepareSequenceValueWithSelfAdjust(ruleName, appliedResult);
                            }
                            if (result != null) {
                                return result;
                            }
                            result = prepareSequenceValueWhitWhitelist(ruleName);
                            if (result != null) {
                                return result;
                            }

                            if (!isElastic) {
                                result = prepareSequenceValueWithPlanned(ruleName, appliedResult);
                                if (result != null) {
                                    return result;
                                }
                            }

                            SequenceRange sequenceRange = populateCombinateSequenceRange(appliedResult);
                            result = applySequenceRule(sequenceRange, combinationRule, 0);
                            DBType dbType = sequenceDao.getDataSource().getDBTypeByLogicalId(
                                sequenceRange.getLogicDBIndex());
                            result.setDbType(dbType);
                            return result;
                        } catch (Exception e) {
                            throw new CombinationSequenceException("CombinationSequence Error:"
                                                                   + e.getMessage(), e);
                        } finally {
                            finishZdalSequenceOperation();
                        }
                    } else {
                        String realSQL = getRealDBMeshSequenceSQL(DBMeshSequenceSQL.SELECT_EID_SQL,
                            sequenceName);

                        Connection conn = null;
                        PreparedStatement ps = null;
                        ResultSet rs = null;
                        try {
                            prepareDBMeshSequenceOperation(sequenceDao.getDataSource()
                                .isUseDbMesh(), zdalInitStatusDBMesh);
                            prepareDBMeshSequenceRule(useDbMesh, zdal2DBMeshUsingDBMesh,
                                shardingParameters, true);
                            // get disaster status from ThreadLocal
                            String disasterStatus = BusinessTransactionContext.getDisasterStatus() == null ? ""
                                : BusinessTransactionContext.getDisasterStatus().name();
                            if (StringUtils.isEmpty(disasterStatus)
                                && !StringUtils.isEmpty(BusinessTransactionContext.getZoneUid())) {
                                // get disaster status from ZoneClient
                                disasterStatus = ZdalZoneUtil
                                    .getDisasterStatus(BusinessTransactionContext.getZoneUid());
                            }

                            if (StringUtils.isNotEmpty(disasterStatus)
                                && !ZdalZoneUtil.NORMAL_STATUS.equals(disasterStatus)) {
                                if (logger.isInfoEnabled()) {
                                    logger.info("self adjust use disaster status: "
                                                + disasterStatus);
                                }
                                realSQL = "/*ODP: DISASTER_STATUS=" + disasterStatus + " */"
                                          + realSQL;
                            }

                            if (sequenceDao instanceof IBatisSequenceDao) {
                                IBatisSequenceDao iBatisSequenceDao = (IBatisSequenceDao) sequenceDao;
                                String tntId = iBatisSequenceDao.lookupTntId();
                                if (iBatisSequenceDao.isEnableTntId() && tntId != null) {
                                    realSQL = "/*ODP: TNT_ID=" + tntId + " */" + realSQL;
                                }
                            }

                            conn = sequenceDao.getDataSource().getConnection();
                            ps = conn.prepareStatement(realSQL);

                            dealWithDBMeshSequenceParameters(shardingColumns, shardingParameters,
                                ps);

                            rs = ps.executeQuery();
                            if (rs.next()) {
                                String eid = rs.getString("eid");
                                String groupId = rs.getString("groupid");
                                String tableId = rs.getString("tableid");
                                ISequenceCombinationRule combinationRule = getSequenceRuleMap()
                                    .get(ruleName);
                                result = combinationRule.applyCombinationRule(null, eid, groupId,
                                    tableId, 0);
                                result.setDbType(DBType.DBMESH);
                                return result;
                            }

                            throw new CombinationSequenceException(
                                "CombinationSequence Error: Prepare sequence value found no response with context :"
                                        + realSQL);

                        } catch (SQLException e) {
                            String msg = "Fail to execute sql: " + realSQL;
                            logger.error(msg, e);
                            throw new CombinationSequenceException(msg, e);
                        } finally {
                            finishDBMeshSequenceOperation();
                            ZdalJdbcUtil.close(rs, ps, conn);
                        }
                    }
                }
            }, "prepareSequenceValue", sequenceName, ruleName, shardingParameters);
        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }
            logger.error("Generate sequence error", e);
            return null;
        }
    }

    private SequenceResult prepareSequenceValueWithPlanned(String ruleName,
                                                           RuleAppliedResult appliedResult)
                                                                                           throws Exception {
        List<String> elasticList;
        try {
            if (!BusinessTransactionContext.isPlannedElasticBusiness()) {
                return null;
            }
            if (!ZdalZoneUtil.isPlannedElasticUid(BusinessTransactionContext.getZoneUid())) {
                return null;
            }

            elasticList = ZdalZoneUtil.getElasticList();
            // cloud be null,not trust worthy
            if (CollectionUtils.isEmpty(elasticList)) {
                return null;
            }
            ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(ruleName);
            SequenceRange sequenceRange = populateCombinateSequenceRangeByElasticIds(
                appliedResult, elasticList, false);
            SequenceResult result = applySequenceRule(sequenceRange, combinationRule, 0);
            DBType dbType = sequenceDao.getDataSource().getDBTypeByLogicalId(
                sequenceRange.getLogicDBIndex());
            result.setDbType(dbType);
            if (result != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("elastic mode successfully find logic db : "
                                 + sequenceRange.getLogicDBIndex() + " in elasticIndex = "
                                 + sequenceRange.getElasticDataSourceIndex());
                }
            }
            return result;
        } catch (Exception e) {
            // 计划弹如果获取有异常, 不能直接抛异常, 需要走到正常非弹 eid
            logger
                .warn("WARN ## prepareSequenceValueWithPlanned can't find elastic dataSource, but will not throw Exception:"
                      + e.getMessage());
            return null;
        }
    }

    private SequenceResult prepareSequenceValueWithElastic(String ruleName,
                                                           RuleAppliedResult appliedResult)
                                                                                           throws Exception {
        List<String> elasticList;
        // in status check , will return null, will not throw Exception
        try {
            if (StringUtils.isEmpty(BusinessTransactionContext.getZoneUid())) {
                return null;
            }
            if (!UidRange.getInstance().isInitialized())
                return null;
            if (UidRange.getInstance().isInRange(BusinessTransactionContext.getZoneUid())) {// is it in a elastic zone
                return null;
            }

            elasticList = ZdalZoneUtil.getElasticList();
            // cloud be null,not trust worthy
            if (CollectionUtils.isEmpty(elasticList)) {
                return null;
            }
        } catch (Exception e) {
            logger
                .warn("WARN ## prepareSequenceValueWithElastic status check failed, will not throw Exception:"
                      + e.getMessage());
            return null;
        }
        try {
            // 弹性白名单优先级高于弹性 sequence 规则
            SequenceResult result = prepareSequenceValueWhitWhitelist(ruleName);
            if (result != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("elastic mode successfully find white list by: "
                                 + BusinessTransactionContext.getWhiteListUserId());
                }
                return result;
            }

            // 没有找到弹性白名单规则，则走弹性 sequence 规则
            ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(ruleName);
            SequenceRange sequenceRange = populateCombinateSequenceRangeByElasticIds(
                appliedResult, elasticList, false);
            result = applySequenceRule(sequenceRange, combinationRule, 0);
            DBType dbType = sequenceDao.getDataSource().getDBTypeByLogicalId(
                sequenceRange.getLogicDBIndex());
            result.setDbType(dbType);
            if (result != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("elastic mode successfully find logic db : "
                                 + sequenceRange.getLogicDBIndex() + " in elasticIndex = "
                                 + sequenceRange.getElasticDataSourceIndex());
                }
            } else {
                throw new GroupDataSourceSelectionException(
                    "can't find elastic dataSource!, will throw Exception");
            }
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Prepare sequence value with self adjust mode
     */
    private SequenceResult prepareSequenceValueWithSelfAdjust(String ruleName,
                                                              RuleAppliedResult appliedResult) {
        // get disaster status from ThreadLocal
        String disasterStatus = BusinessTransactionContext.getDisasterStatus() == null ? ""
            : BusinessTransactionContext.getDisasterStatus().name();
        if (StringUtils.isEmpty(disasterStatus)) {
            // get disaster status from ZoneClient
            if (StringUtils.isEmpty(BusinessTransactionContext.getZoneUid())) {
                return null;
            }
            disasterStatus = ZdalZoneUtil
                .getDisasterStatus(BusinessTransactionContext.getZoneUid());
        }

        if (StringUtils.isEmpty(disasterStatus) || ZdalZoneUtil.NORMAL_STATUS.equals(disasterStatus)) {
            return null;
        }

        if (logger.isInfoEnabled()) {
            logger.info("self adjust use disaster status: " + disasterStatus);
        }

        // get elastic id by disaster status
        List<String> targetElasticIds = sequenceDao.getDataSource().getZdalDataSourceConfig()
            .getLabelToElasticIdMap().get(disasterStatus);
        List<String> elasticList = ZdalZoneUtil.getElasticList();
        if (targetElasticIds != null && elasticList != null) { // 排除弹性机房的 eid
            targetElasticIds.removeAll(elasticList);
        }
        if (CollectionUtils.isEmpty(targetElasticIds)) {
            if (logger.isInfoEnabled()) {
                logger.info("self adjust cannot find disaster elastic id: " + disasterStatus);
            }
            return null;
        }

        ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(ruleName);
        try {
            SequenceRange sequenceRange = populateCombinateSequenceRangeByElasticIds(
                appliedResult, targetElasticIds, true);
            SequenceResult result = applySequenceRule(sequenceRange, combinationRule, 0);
            DBType dbType = sequenceDao.getDataSource().getDBTypeByLogicalId(
                sequenceRange.getLogicDBIndex());
            result.setDbType(dbType);
            if (result != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("self adjust successfully find logic db : "
                                + sequenceRange.getLogicDBIndex() + " in elasticIndex = "
                                + sequenceRange.getElasticDataSourceIndex());
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("Failed to load self adaptive db from context, weight will be used!", e);
            return null;
        }
    }

    private SequenceResult prepareSequenceValueWhitWhitelist(String ruleName) {

        ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(ruleName);
        // If the routing has been given, using it to generate sequence value
        String routingLogicalDataSourceId = resolveWithWhiteList();
        // check the value
        if (!StringUtils.isEmpty(routingLogicalDataSourceId)) {
            SequenceResult result = applySequenceRule(
                populateCombinateSequenceRangeWithUserId(routingLogicalDataSourceId),
                combinationRule, 0);
            result.setDbType(sequenceDao.getDataSource().getDBTypeByLogicalId(
                routingLogicalDataSourceId));
            return result;
        }

        return null;
    }

    private SequenceResult applySequenceRule(SequenceRange sequenceRange,
                                             ISequenceCombinationRule combinationRule, long value) {
        // save current disaster status in
        String elasticDataSourceIndex = sequenceRange.getElasticDataSourceIndex();
        BusinessTransactionContext.setCurrentDisasterStatus(DisasterStatusEnum.NORMAL);
        if (elasticDataSourceIndex != null) {
            XDataSource zdalDataSource = sequenceDao.getDataSource();
            Map<String, String> groupLabelMap = zdalDataSource.getZdalDataSourceConfig()
                .getGroupLabelMap();
            int eid = Integer.valueOf(elasticDataSourceIndex);
            // 因为存在 groupLabelMap 里面是两位数字，如 00，01，但是拿到的 elasticDataSourceIndex 只是 0 或者 1
            // 所以遍历一次将数据转换为 Integer 对数据进行对比
            for (Map.Entry<String, String> entry : groupLabelMap.entrySet()) {
                int eidConfig = Integer.parseInt(entry.getKey());
                String label = entry.getValue();
                if (eidConfig == eid) { // 之所以
                    BusinessTransactionContext.setCurrentDisasterStatus(DisasterStatusEnum
                        .valueOf(label));
                    break;
                }
            }
        }

        return combinationRule.applyCombinationRule(sequenceRange, value);
    }

    @Override
    @Deprecated(since = "0")
    public SequenceResult prepareSequenceValue(String ruleName, String ruleString, String uid)
                                                                                              throws DataAccessException {
        return prepareSequenceValue(ruleName, ruleString, uid, defaultShardingParameters);
    }

    /**
     *
     * @param ruleName              real or virtual
     * @param ruleString            the content of routing rule
     * @param uid
     * @param shardingParameters    the key of shard db/table
     * @return
     * @throws DataAccessException
     */
    @Override
    @Deprecated(since = "0")
    public SequenceResult prepareSequenceValue(final String ruleName, final String ruleString,
                                               final String uid,
                                               final List<Object> shardingParameters)
                                                                                     throws DataAccessException {
        Preconditions.checkNotNull(ruleName, "ruleName is required.");
        Preconditions.checkNotNull(uid, "uid is required.");

        try {
            return AmbushZdalUtil.invokeSequence(new GenericCodeWrapper<SequenceResult>() {
                @Override
                public SequenceResult call() throws Exception {

                    boolean useDbMesh = sequenceDao.getDataSource().isUseDbMesh();
                    boolean zdal2DBMeshUsingDBMesh = sequenceDao.getDataSource()
                        .getZdalDataSourceConfig().getAttributesConfig().isZdal2DBMeshUsingDBMesh();
                    if (!useDbMesh && !zdal2DBMeshUsingDBMesh) {
                        try {
                            prepareZdalSequenceOperation();
                            ISequenceCombinationRule combinationRule = getSequenceRuleMap().get(
                                ruleName);
                            SequenceResult result;
                            try {
                                OneshotRuleMatcher matcher = new OneshotRuleMatcher(ruleString);
                                String routingLogicalDataSourceId = matcher.find(uid);
                                if (!StringUtils.isBlank(routingLogicalDataSourceId)) {
                                    result = applySequenceRule(
                                        populateCombinateSequenceRangeWithUserId(routingLogicalDataSourceId),
                                        combinationRule, 0);
                                    result.setDbType(sequenceDao.getDataSource()
                                        .getDBTypeByLogicalId(routingLogicalDataSourceId));
                                    return result;
                                }
                            } catch (Exception e) {
                                logger.warn("Illegal ruleString.{" + ruleString + "}.clause:"
                                            + e.getMessage());
                            }

                            try {
                                RuleAppliedResult appliedResult = sequenceDao
                                    .applySequenceTableRule(shardingParameters);
                                SequenceRange sequenceRange = populateCombinateSequenceRange(appliedResult);
                                result = combinationRule.applyCombinationRule(sequenceRange, 0);
                                DBType dbType = sequenceDao.getDataSource()
                                    .getDBTypeByLogicalId(sequenceRange.getLogicDBIndex());
                                result.setDbType(dbType);
                                return result;
                            } catch (Exception e) {
                                throw new CombinationSequenceException("CombinationSequence Error:"
                                                                       + e.getMessage(), e);
                            }
                        } finally {
                            finishZdalSequenceOperation();
                        }
                    } else {

                        throw new CombinationSequenceException(
                            "CombinationSequence Error: not supported yet.");
                    }
                }
            }, "prepareSequenceValue", ruleName, ruleString, uid, shardingParameters);
        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                throw (DataAccessException) e;
            }
            logger.error("Generate sequence error", e);
            return null;
        }

    }

    private String resolveWithWhiteList() {
        if (StringUtils.isNotBlank(BusinessTransactionContext.getWhiteListUserId())) {
            String userId = BusinessTransactionContext.getWhiteListUserId();
            String userDefined = BusinessTransactionContext.getContext().getUserDefined();

            String matchSource = userId;
            if (!StringUtils.isEmpty(userDefined)) {
                matchSource += IWhiteListRule.EXPRESSION_SEPARATOR + userDefined;
            }
            if (sequenceDao.getDataSource().getRuleMatcher() != null) {
                return sequenceDao.getDataSource().getRuleMatcher().find(matchSource);
            }
        }
        return null;
    }

    /**
     * According to current data source weight to calculate target database and table
     *
     * @param ruleAppliedResult
     * @return
     */
    private SequenceRange populateCombinateSequenceRange(RuleAppliedResult ruleAppliedResult) {
        return populateCombinateSequenceRangeByElasticIds(ruleAppliedResult, null, false);
    }

    /**
     * According to userId to calculate target database
     *
     * @return
     */
    private SequenceRange populateCombinateSequenceRangeWithUserId(String logicalDataSourceId) {
        SequenceRange priorSequenceRange = new SequenceRange();
        XDataSource zdalDataSource = sequenceDao.getDataSource();
        DataSourceElasticInfo elasticInfo = zdalDataSource.getElasticInfoMap().get(logicalDataSourceId);
        try {
            if (zdalDataSource.isRuleCompatible()) {
                priorSequenceRange.setTableShardValue(String.valueOf(elasticInfo.getElasticNum()));
            } else {
                priorSequenceRange
                    .setDatabaseShardValue(String.valueOf(elasticInfo.getElasticNum()));
            }
            priorSequenceRange.setElasticDataSourceIndex(String.valueOf(elasticInfo.getElasticIndex()));
        } catch (Exception e) {
            logger.error("Calculate elastic rule error", e);
            throw new IllegalArgumentException("Calculate elastic rule error", e);
        }
        return priorSequenceRange;
    }

    /**
     * Pupulate CombinateSequenceRange by group elastic id list
     *
     * @param ruleAppliedResult sharding results
     * @param elasticList       elastic id list
     * @return target sequence range
     */
    private SequenceRange populateCombinateSequenceRangeByElasticIds(RuleAppliedResult ruleAppliedResult,
                                                                     List<String> elasticList,
                                                                     boolean selfAdjust) {
        SequenceRange priorSequenceRange = new SequenceRange();
        // route to ElaticDataSource
        GroupDataSource dataSource = applyRuleForElasticDataSource(priorSequenceRange,
            ruleAppliedResult);

        try {
            DataSourceReadWriteWeight weight;
            if (elasticList != null && !elasticList.isEmpty()) {
                weight = dataSource.getGroupDataSourceWeight().findLogicalDataSourceInElasticMode(
                    elasticList, selfAdjust);
            } else {
                elasticList = ZdalZoneUtil.getElasticList();

                if (elasticList != null && !elasticList.isEmpty()) {
                    Set<String> failedList = new HashSet<String>();
                    for (String eid : elasticList) {
                        try {
                            int eIndex = Integer.valueOf(eid);
                            int idxTemp = dataSource.getGroupDataSourceWeight()
                                .searchAtomWeightConfigIndex(eIndex);
                            if (idxTemp != -1)
                                failedList.add(dataSource.getGroupDataSourceWeight()
                                    .getAtomDataSourceWeights().get(idxTemp).getLogicDsId());
                        } catch (Exception e) {
                            logger.warn(e.getMessage() + " , but will not affect prepare");
                        }
                    }
                    // will not choose weight which in elastic mode
                    weight = dataSource.getGroupDataSourceWeight().findLogicalDataSource(false,
                        null, failedList, selfAdjust);
                } else {
                    weight = dataSource.getGroupDataSourceWeight().findLogicalDataSource(false,
                        null, null, selfAdjust);
                }
            }

            priorSequenceRange.setElasticDataSourceIndex(String.valueOf(weight.getIndex()));
            priorSequenceRange.setLogicDBIndex(weight.getLogicDsId());
        } catch (Exception e) {
            if (CollectionUtils.isEmpty(elasticList)) {
                logger.error("Calculate elastic rule error", e);
            }
            throw new IllegalArgumentException("Calculate elastic rule error", e);
        }
        return priorSequenceRange;
    }

    private ElasticDataSource applyRuleForElasticDataSource(SequenceRange sequenceRange,
                                                            RuleAppliedResult ruleAppliedResult) {
        TargetDatabasesAndTables targetDb = null;
        XDataSource zdalDataSource = sequenceDao.getDataSource();
        for (TargetDatabasesAndTables target : ruleAppliedResult.getTarget()) {
            targetDb = target;
            sequenceRange.setDatabaseShardValue(targetDb.getDatabaseShardValue());
            sequenceRange.setTableShardValue(targetDb.getTableShardValue());
            sequenceRange.setDatabaseIndex(target.getDbIndex());
            break;
        }
        assert targetDb != null;
        for (String tableName : targetDb.getTableNames()) {
            sequenceRange.setLogicalTableName(ruleAppliedResult.getVirtualTableName());
            DBSelectorIDRouteCondition routeCondition = new DBSelectorIDRouteCondition(
                ruleAppliedResult.getVirtualTableName(), targetDb.getDbIndex(), tableName);
            sequenceRange.setRouteCondition(routeCondition);
            break;
        }
        int groupIndex = findElasticDataSourceId(targetDb);
        if (groupIndex > zdalDataSource.getElasticDataSourceList().size() || groupIndex < 0) {
            throw new IllegalArgumentException("Calculate elastic rule error: The table index "
                                               + targetDb.getTableShardValue()
                                               + " is out of size of elastic data source group.");
        }
        sequenceRange.setTableShardValue(String.valueOf(groupIndex));
        return zdalDataSource.getElasticDataSourceList().get(groupIndex);
    }

    /**
     * Base on setting either by table or database to find out elastic data source id
     */
    private int findElasticDataSourceId(TargetDatabasesAndTables targetDb) {
        int elasticDataSourceId = -1;
        if (sequenceDao.getDataSource().isRuleCompatible()) {
            if (!StringUtils.isEmpty(targetDb.getTableShardValue())) {
                try {
                    elasticDataSourceId = Integer.parseInt(targetDb.getTableShardValue());
                } catch (Exception e) {
                    logger.warn("ZDAL Sequence failed to retrieve elastic data source index from "
                                + targetDb.getTableShardValue(), e);
                }
            }
        } else {
            if (!StringUtils.isEmpty(targetDb.getDatabaseShardValue())) {
                try {
                    elasticDataSourceId = Integer.parseInt(targetDb.getDatabaseShardValue());
                } catch (Exception e) {
                    logger.warn("ZDAL Sequence failed to retrieve elastic data source index from "
                                + targetDb.getDatabaseShardValue(), e);
                }
            }
        }
        return elasticDataSourceId;
    }

    public void refreshSequenceStepValue(int drmPushedStepValue) {
        sequenceDao.refreshGlobalStepValue(drmPushedStepValue);
    }

    public String getCombinationSequenceResourceId() {
        return combinationSequenceResourceId;
    }

    public void setCombinationSequenceResourceId(String combinationSequenceResourceId) {
        this.combinationSequenceResourceId = combinationSequenceResourceId;
    }

    @Deprecated
    @SuppressWarnings("unused")
    public void setSequenceNameSet(Set<String> sequenceNameSet) {
    }

    private void prepareZdalSequenceOperation() {
        SequenceContext.enterSequenceDaoFlag();
        DBMeshContextThreadLocal.setForceUseNonMeshPhysicalDataSourceFlag(true);
        DBMeshContextThreadLocal.setForceUseZdalDataSourceFlag(true);
    }

    private void finishZdalSequenceOperation() {
        SequenceContext.clearSequenceDaoFlag();
        DBMeshContextThreadLocal.setForceUseNonMeshPhysicalDataSourceFlag(false);
        DBMeshContextThreadLocal.setForceUseZdalDataSourceFlag(false);
    }

    private void prepareDBMeshSequenceOperation(boolean useDbMesh, boolean zdalInitStatusDBMesh) {

        if (useDbMesh && zdalInitStatusDBMesh) {
            throw new CombinationSequenceException(
                "CombinationSequence Error: Illegal Argument useDbMesh is conflict with zdal2DBMeshStatus ["
                        + sequenceDao.getDataSource().getZdalDataSourceConfig()
                            .getAttributesConfig().getZdal2DBMeshStatus() + "] for appName ["
                        + sequenceDao.getDataSource().getAppName() + "] appDsName ["
                        + sequenceDao.getDataSource().getAppDataSourceName() + "]");
        }

        SequenceContext.enterSequenceDaoFlag();

        if (useDbMesh) {
            DBMeshContextThreadLocal.setForceUseNonMeshPhysicalDataSourceFlag(true);
            DBMeshContextThreadLocal.setForceUseZdalDataSourceFlag(true);
            return;
        }

        if (zdalInitStatusDBMesh) {
            DBMeshContextThreadLocal.setForceUseNonMeshPhysicalDataSourceFlag(true);
            DBMeshContextThreadLocal.setForceUseDbmeshDataSourceFlag(true);
            return;
        }

        DBMeshContextThreadLocal.setForceUseMeshPhysicalDataSourceFlag(true);
        DBMeshContextThreadLocal.setForceUseZdalDataSourceFlag(true);
    }

    private void prepareDBMeshSequenceRule(boolean useDbMesh, boolean zdal2DBMeshUsingDBMesh,
                                           List<Object> parameters, boolean ignoreElasticId)
                                                                                            throws Exception {

        if (useDbMesh && zdal2DBMeshUsingDBMesh) {
            throw new CombinationSequenceException(
                "CombinationSequence Error: Illegal Argument useDbMesh is conflict with zdal2DBMeshUsingDBMesh for appName ["
                        + sequenceDao.getDataSource().getAppName()
                        + "] appDsName ["
                        + sequenceDao.getDataSource().getAppDataSourceName() + "]");
        }

        XDataSource zdalDataSource = zdal2DBMeshUsingDBMesh ? sequenceDao
            .getDataSource() : sequenceDao.getDataSource().getParentDataSource();
        boolean isParentUseZdalRule = zdalDataSource != null
                                      && zdalDataSource.getZdalDataSourceConfig()
                                          .getAttributesConfig().isUseZdalRule();
        boolean isCurrentInitStatusDynamic = sequenceDao.getDataSource() != null
                                             && sequenceDao.getDataSource()
                                                 .getZdalDataSourceConfig() != null
                                             && sequenceDao.getDataSource()
                                                 .getZdalDataSourceConfig().getAttributesConfig()
                                                 .isZdalInitStatusDynamic();

        boolean needComputeRule = isCurrentInitStatusDynamic || isParentUseZdalRule;

        RuleAppliedResult ruleAppliedResult;

        if (needComputeRule) {
            if (zdal2DBMeshUsingDBMesh) {
                ruleAppliedResult = sequenceDao.applySequenceTableRule(parameters);
            } else {
                throw new CombinationSequenceException(
                    "CombinationSequence Error: Illegal Argument pure DBMesh data source must not set useZdalRule true for appName ["
                            + sequenceDao.getDataSource().getAppName()
                            + "] appDsName ["
                            + sequenceDao.getDataSource().getAppDataSourceName() + "]");
            }

            if (ruleAppliedResult.getTarget() == null || ruleAppliedResult.getTarget().size() == 0) {
                throw new RuntimeException(
                    "CombinationSequence Error: Sequence table rule calculate is error. target is null! shardingParameters="
                            + parameters);
            }

            if (ruleAppliedResult.getTarget().size() != 1) {
                throw new RuntimeException(
                    "CombinationSequence Error: Sequence table rule calculate is error. Multi target is now allowed! shardingParameters="
                            + parameters);
            }

            TargetDatabasesAndTables target = ruleAppliedResult.getTarget().get(0);

            DBMeshContext dispatchContext = new DBMeshContext();

            if (StringUtils.isNotEmpty(target.getTableShardValue())
                && StringUtils.isNumeric(target.getTableShardValue())) {
                dispatchContext.setTableId(Integer.valueOf(target.getTableShardValue()));
            } else if (target.getTableNames().size() > 0) {
                dispatchContext.setTableName(target.getTableNames().get(0));
            }

            if (StringUtils.isNotEmpty(target.getDatabaseShardValue())) {
                dispatchContext.setGroupId(Integer.valueOf(target.getDatabaseShardValue()));
            }

            if (!target.getTablePartitions().isEmpty() && target.getTableNames().size() > 0) {
                dispatchContext.setPartKey(target.getTablePartitions()
                    .get(target.getTableNames().get(0)).pickOnePartition());
            }

            // be careful about the case where the elastic rule value is -1. when the elastic rule is -1 ,which means the elastic rule is empty ,
            // we do not need to force odp to use weight.
            if (!ignoreElasticId && StringUtils.isNotEmpty(target.getElasticRuleValue())
                && !"-1".equalsIgnoreCase(target.getElasticRuleValue())) {
                dispatchContext.setElasticId(Integer.valueOf(target.getElasticRuleValue()));
            }
            dispatchContext.setZoneUid(BusinessTransactionContext.getZoneUid());
            DBMeshContextThreadLocal.setDBMeshDispatchContext(dispatchContext);

            if (isCurrentInitStatusDynamic) {
                DBSelectorIDRouteCondition routeCondition = new DBSelectorIDRouteCondition(
                    ruleAppliedResult.getVirtualTableName(), target.getDbIndex());
                routeCondition.setDatabaseShardValue(target.getDatabaseShardValue());
                routeCondition.setElasticDataSourceIndex(target.getElasticRuleValue());
                routeCondition.setDataSourceIndex(target.getElasticRuleValue());
                routeCondition.setTableShardValue(target.getTableShardValue());
                if (!target.getTablePartitions().isEmpty()) {
                    routeCondition.setPartitionId(target.getTablePartitions()
                        .get(target.getTableNames().get(0)).pickOnePartition());
                }
                ThreadLocalMap.put(ThreadLocalString.SEQUENCE_ROUTE_CONDITION, routeCondition);
            }
        }
    }

    private void dealWithDBMeshSequenceParameters(List<String> shardingColumns,
                                                  List<Object> shardingParameters,
                                                  PreparedStatement ps) throws SQLException {
        for (int i = 0; i < shardingColumns.size(); i++) {
            if (i >= shardingParameters.size()) {
                ps.setObject(i + 1, null);
            }
            ps.setObject(i + 1, shardingParameters.get(i));
        }
    }

    private void finishDBMeshSequenceOperation() {
        SequenceContext.clearSequenceDaoFlag();
        DBMeshContextThreadLocal.setForceUseMeshPhysicalDataSourceFlag(false);
        DBMeshContextThreadLocal.setForceUseNonMeshPhysicalDataSourceFlag(false);
        DBMeshContextThreadLocal.setForceUseZdalDataSourceFlag(false);
        DBMeshContextThreadLocal.setForceUseDbmeshDataSourceFlag(false);
    }

    public List<String> getShardingColumns() {
        return shardingColumns;
    }

    public void setShardingColumns(String shardingColumns) {
        if (StringUtils.isEmpty(shardingColumns))
            return;
        String[] columnArray = StringUtils.split(shardingColumns.trim(), ",");
        for (String columnName : columnArray) {
            this.shardingColumns.add(columnName.trim());
        }
    }
}
