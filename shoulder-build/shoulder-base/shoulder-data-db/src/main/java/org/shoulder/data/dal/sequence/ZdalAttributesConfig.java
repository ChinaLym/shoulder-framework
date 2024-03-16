package org.shoulder.data.dal.sequence;

import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.StringUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author lym
 */
public class ZdalAttributesConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("CLIENT");

    /**
     * OCJ2OBPROXY 物理数据源初始化状态：
     *    DYNAMIC表示可以在OCJ/OBPROXY之间动态切换，会同时加载 OCEANBASE/OB_CLOUD 数据源
     *    OBPROXY表示只初始化OBPROXY数据源。只要是 OCEANBASE(20) 的库，都会直接切换成 OB_CLOUD(20) 的库，提升启动速度
     *    OFF表示保持原样，默认值
     */
    enum DsInitStatus {
        OFF, DYNAMIC, OBPROXY
    }

    /**
     * zdal2DBMeshZeroMinConn 物理数据源初始化最小连接数状态: 仅在 zdal2DBMeshStatus 为 Dynamic 时候生效
     *    DBMESH 表示仅初始化 DBMESH 数据源最小连接数
     *    ZDAL   表示仅初始化 ZDAL 数据源最小连接数
     *    OFF   表示 DBMESH 和 ZDAL 数据源最小连接数都初始化
     */
    enum DsInitMinConnStatus {
        DBMESH, ZDAL, OFF
    }

    /**
     * ZDAL2DBMESH 数据源初始化状态：
     *    DYNAMIC 表示可以在ZDAL/DBMESH之间动态切换，会同时加载 ZDAL/DBMESH 两种数据源
     *    DBMESH  表示只初始化DBMESH数据源
     *    OFF     表示保持原样，默认值
     */
    enum ZdalInitStatus {
        OFF, DYNAMIC, DBMESH
    }

    /**
     * 是否开启 DB 透传 Trace 信息的功能
     *
     * 默认不开启
     */
    private boolean                                       dbTrace                        = true;

    /**
     * Sequence 刷新功能是否开启
     *
     * 默认不开启
     */
    private boolean                                       sequenceRefresh                = false;

    /**
     * Sequence 刷新间隔时间
     *
     * 默认 10000 ms
     */
    private long                                          sequenceRefreshInterval        = 10 * 1000;

    /**
     * Sequence 刷新阈值，当前 SequenceRange 超过这个阈值会刷新下一个 SequenceRange
     *
     * 默认 30% 时就刷新下一个 SequenceRange
     */
    private double                                        sequenceRefreshThreshold       = 0.3;

    /**
     * Sequence 日志打印间隔时间
     *
     * 默认 30000 ms
     */
    private long                                          sequenceLogPrintInterval       = 30 * 1000;

    /**
     * 事务提交延迟模拟
     *
     * 格式：DBMODE#mode;GLOBAL#10;ZONE#GZ:10;DBKEY#xxhost:10|DBMODE#mode;GLOBAL#10;ZONE#GZ:10;DBKEY#xxhost:10
     * 优先级：DBKEY > ZONE > GLOBAL
     */
    private String                                        txCommitLatencyThreshold       = null;

    private long                                          globalLatency                  = 0;
    private volatile Map<String, Long>                    dbkeyLatencyMap;
    // TreeMap key ordered by ASCII asc
    private volatile TreeMap<String, Long>                zoneLatencyMap;

    /**
     * 自治批量 SQL(匿名块) 开关
     *
     * 格式：DBMODE#mode;GLOBAL#ON;ZONE#GZ#ON;DBKEY#xxhost#ON|DBMODE#mode;GLOBAL#ON;ZONE#GZ#ON;DBKEY#xxhost#ON
     * 优先级：DBKEY > ZONE > GLOBAL
     */
    private String                                        useAutoBatchSqlSwitch          = null;

    private boolean                                       globalUseAutoBatchSql          = false;
    private volatile Map<String, Boolean>                 dbkeyUseAutoBatchSqlMap;
    // TreeMap key ordered by ASCII asc
    private volatile TreeMap<String, Boolean>             zoneUseAutoBatchSqlMap;

    /**
     * OCJ 切换 ObProxy 的数据源初始化状态
     *
     * 格式：DBMODE#mode1,mode2;TYPE#KEY:DYNAMIC/OBPROXY/OFF;|DBMODE#mode;TYPE#KEY:DYNAMIC/OBPROXY/OFF
     * TYPE：
     *    DBMODE, 可以控制不同环境的配置。| 作为分隔符
     *    GLOBAL，全局初始化开关，不可动态切换；
     *    ZONE，ZONE级别数据源初始化开关，不可动态切换；
     *    DBKEY，统一数据源级别初始化开关，不可动态切换
     * KEY：GLOBAL没有KEY；ZONE的KEY是LDC ZONE信息，可以只写前缀，如GZ；DBKEY的KEY是统一数据源的名字
     * 优先级：DBKEY > ZONE > GLOBAL
     * 样例：
     * GLOBAL#DYNAMIC;ZONE#GZ:OBPROXY;DBKEY#xxhost:OBPROXY;DBKEY#yyhost:OFF
     */
    private String                                        ocj2ObProxyStatus              = "GLOBAL#OBPROXY";
    private DsInitStatus                                  globalOcj2ObProxyStatus        = DsInitStatus.OBPROXY;
    private volatile Map<String, DsInitStatus>            dbkeyOcj2ObProxyStatusMap      = new HashMap<String, DsInitStatus>();
    // TreeMap key ordered by ASCII asc
    private volatile TreeMap<String, DsInitStatus>        zoneOcj2ObProxyStatusMap       = new TreeMap<String, DsInitStatus>();
    private AtomicBoolean                                 ocj2ObProxyStatusInited        = new AtomicBoolean(
        false);

    /**
     * OCJ 切换 ObProxy 的开关
     *
     * 格式：DBMODE#mode1,mode2;TYPE#KEY:ON/OFF;|DBMODE#mode;TYPE#KEY:ON/OFF
     * TYPE：
     *    DBMODE, 可以控制不同环境的配置。| 作为分隔符
     *    GLOBAL，全局切换开关，可动态切换；
     *    ZONE，ZONE级别切换开关，可动态切换；
     *    DBKEY，统一数据源级别切换开关，可动态切换
     * KEY：GLOBAL没有KEY；ZONE的KEY是LDC ZONE信息，可以只写前缀，如GZ；DBKEY的KEY是统一数据源的名字
     * 优先级：DBKEY > ZONE > GLOBAL
     * 样例：
     * STATUS#DYNAMIC;GLOBAL#ON;ZONE#GZ:ON;DBKEY#xxhost:ON;DBKEY#yyhost:ON
     * STATUS#OBPROXY
     */
    private String                                        ocj2ObProxySwitch              = null;

    private boolean                                       globalOcj2ObProxySwitch        = false;
    private volatile Map<String, Boolean>                 dbkeyOcj2ObProxySwitchMap;
    // TreeMap key ordered by ASCII asc
    private volatile TreeMap<String, Boolean>             zoneOcj2ObProxySwitchMap;

    /**
     * ZDAL 切换 DBMESH 的数据源初始化状态
     *
     * 格式：DBMODE#mode1,mode2;TYPE#KEY:DYNAMIC/DBMESH/OFF;|DBMODE#mode;TYPE#KEY:DYNAMIC/OBPROXY/OFF
     * TYPE：
     *    DBMODE, 可以控制不同环境的配置。| 作为分隔符
     *    GLOBAL，全局初始化开关，不可动态切换；
     *    ZONE，ZONE级别数据源初始化开关，不可动态切换；
     * KEY：GLOBAL没有KEY；ZONE的KEY是LDC ZONE信息，可以只写前缀，如GZ；IP的KEY是机器的名字
     * 优先级：ZONE > GLOBAL
     * 样例：
     * GLOBAL#DYNAMIC;ZONE#GZ:DBMESH
     */
    private String                                        zdal2DBMeshStatus              = null;
    private volatile ZdalInitStatus                       zdal2DBMeshStatusJudge         = ZdalInitStatus.OFF;
    private AtomicBoolean                                 zdal2DBMeshStatusInited        = new AtomicBoolean(
        false);

    /**
     * ZDAL 切换 DBMESH 的数据初始化时最小连接数的设置,仅在 zdal2DBMeshStatus 是 Dynamic 时生效
     *
     * 格式：DBMODE#mode1,mode2;TYPE#KEY:DBMESH/ZDAL/OFF;|DBMODE#mode;TYPE#KEY:DBMESH/ZDAL/OFF
     * TYPE：
     *    DBMODE, 可以控制不同环境的配置。| 作为分隔符
     *    GLOBAL，全局初始化开关，不可动态切换；
     *    ZONE，ZONE级别数据源初始化开关，不可动态切换；
     *    DBKEY，统一数据源级别初始化开关，不可动态切换
     * KEY：GLOBAL没有KEY；ZONE的KEY是LDC ZONE信息，可以只写前缀，如GZ；DBKEY的KEY是统一数据源的名字
     * 优先级：DBKEY > ZONE > GLOBAL
     * 样例：
     * GLOBAL#OFF;ZONE#GZ:DBMESH;DBKEY#xxhost:ZDAL;DBKEY#yyhost:DBMESH
     */
    private String                                        zdal2DBMeshZeroMinConn         = null;
    private DsInitMinConnStatus                           globalZdal2DBMeshZeroMinConn   = DsInitMinConnStatus.OFF;
    private volatile Map<String, DsInitMinConnStatus>     dbkeyZdal2DBMeshZeroMinConnMap = new HashMap<String, DsInitMinConnStatus>();
    // TreeMap key ordered by ASCII asc
    private volatile TreeMap<String, DsInitMinConnStatus> zoneZdal2DBMeshZeroMinConnMap  = new TreeMap<String, DsInitMinConnStatus>();

    private AtomicBoolean                                 zdal2DBMeshZeroMinConnInited   = new AtomicBoolean(
        false);

    /**
     * ZDAL 切换 DBMESH 的开关
     *
     * 格式：DBMODE#mode1,mode2;TYPE#KEY:ON/OFF;|DBMODE#mode;TYPE#KEY:ON/OFF
     * TYPE：
     *    DBMODE, 可以控制不同环境的配置。| 作为分隔符
     *    GLOBAL，全局切换开关，可动态切换；
     *    ZONE，ZONE级别切换开关，可动态切换；
     * KEY：GLOBAL没有KEY；ZONE的KEY是LDC ZONE信息，可以只写前缀，如GZ；IP的KEY是机器的IP
     * 优先级：ZONE > GLOBAL
     * 样例：
     * STATUS#DYNAMIC;GLOBAL#ON;ZONE#GZ:ON;
     */
    private String                                        zdal2DBMeshSwitch              = null;
    private volatile boolean                              zdal2DBMeshSwitchJudge         = false;

    private volatile boolean                              useZdalRule                    = true;

    /**
     * 是否使用 DBMesh 的模板配置
     */
    private volatile boolean                              useDBMeshTemplate              = false;

    private AtomicBoolean                                 useDBMeshTemplateInited        = new AtomicBoolean(
        false);

    private AtomicBoolean                                 useDBMeshSpecificInit          = new AtomicBoolean(
        false);

    private volatile boolean                              useDBMeshSpecific              = false;
    /**
     * 指定 DBMesh Database 的名称
     */
    private volatile String                               useDBMeshDatabase              = "";

    /**
     * 指定 DBMesh Database 的名称
     */
    private volatile String                               useDBMeshUsername              = "";

    /**
     * 指定 DBMesh Database 的名称
     */
    private volatile String                               useDBMeshPassword              = "";

    public boolean isDbTrace() {
        return dbTrace;
    }

    public void setDbTrace(boolean dbTrace) {
        this.dbTrace = dbTrace;
    }

    public double getSequenceRefreshThreshold() {
        return sequenceRefreshThreshold;
    }

    public void setSequenceRefreshThreshold(double sequenceRefreshThreshold) {
        this.sequenceRefreshThreshold = sequenceRefreshThreshold;
    }

    public long getSequenceRefreshInterval() {
        return sequenceRefreshInterval;
    }

    public void setSequenceRefreshInterval(long sequenceRefreshInterval) {
        this.sequenceRefreshInterval = sequenceRefreshInterval;
    }

    public boolean isSequenceRefresh() {
        return sequenceRefresh;
    }

    public void setSequenceRefresh(boolean sequenceRefresh) {
        this.sequenceRefresh = sequenceRefresh;
    }

    public long getSequenceLogPrintInterval() {
        return sequenceLogPrintInterval;
    }

    public void setSequenceLogPrintInterval(long sequenceLogPrintInterval) {
        this.sequenceLogPrintInterval = sequenceLogPrintInterval;
    }

    public void resetAttributes(ZdalAttributesConfig config) {
        if (config != null) {
            this.dbTrace = config.dbTrace;
            this.sequenceRefreshThreshold = config.sequenceRefreshThreshold;
            this.sequenceRefreshInterval = config.sequenceRefreshInterval;
            this.sequenceRefresh = config.sequenceRefresh;
            this.sequenceLogPrintInterval = config.sequenceLogPrintInterval;

            config.resetTxCommitLatencyThreshold();
            this.txCommitLatencyThreshold = config.txCommitLatencyThreshold;
            this.globalLatency = config.globalLatency;
            this.dbkeyLatencyMap = config.dbkeyLatencyMap;
            this.zoneLatencyMap = config.zoneLatencyMap;

            config.resetUseAutoBatchSqlSwitch();
            this.useAutoBatchSqlSwitch = config.useAutoBatchSqlSwitch;
            this.globalUseAutoBatchSql = config.globalUseAutoBatchSql;
            this.dbkeyUseAutoBatchSqlMap = config.dbkeyUseAutoBatchSqlMap;
            this.zoneUseAutoBatchSqlMap = config.zoneUseAutoBatchSqlMap;

            config.resetOcj2ObProxySwitch();
            this.ocj2ObProxySwitch = config.ocj2ObProxySwitch;
            this.globalOcj2ObProxySwitch = config.globalOcj2ObProxySwitch;
            this.dbkeyOcj2ObProxySwitchMap = config.dbkeyOcj2ObProxySwitchMap;
            this.zoneOcj2ObProxySwitchMap = config.zoneOcj2ObProxySwitchMap;

            config.resetZdal2DBmeshSwitch();
            this.zdal2DBMeshSwitch = config.zdal2DBMeshSwitch;
            this.zdal2DBMeshSwitchJudge = config.zdal2DBMeshSwitchJudge;

            this.useZdalRule = config.useZdalRule;

            if (ocj2ObProxyStatusInited.compareAndSet(false, true)) {
                if (StringUtils.isBlank(config.ocj2ObProxyStatus)) {
                    LOGGER
                        .warn("ocj2ObProxyStatus is blank and it will be converted to GLOBAL#OBPROXY by default ");
                    config.ocj2ObProxyStatus = "GLOBAL#OBPROXY";
                }
                config.resetOcj2ObProxyStatus();
                this.ocj2ObProxyStatus = config.ocj2ObProxyStatus;
                this.globalOcj2ObProxyStatus = config.globalOcj2ObProxyStatus;
                this.dbkeyOcj2ObProxyStatusMap = config.dbkeyOcj2ObProxyStatusMap;
                this.zoneOcj2ObProxyStatusMap = config.zoneOcj2ObProxyStatusMap;
            } else {
                LOGGER
                    .warn(
                        "ocj2ObProxyStatus not support dynamic reset, current status: {}, wanted status: {}",
                        this.ocj2ObProxyStatus, config.ocj2ObProxyStatus);
            }

            if (zdal2DBMeshStatusInited.compareAndSet(false, true)) {
                config.resetZdal2DBMeshStatus();
                this.zdal2DBMeshStatus = config.zdal2DBMeshStatus;
                this.zdal2DBMeshStatusJudge = config.zdal2DBMeshStatusJudge;
            } else {
                LOGGER
                    .warn(
                        "zdal2DBMeshStatus not support dynamic reset, current status: {}, wanted status: {}",
                        this.zdal2DBMeshStatus, config.zdal2DBMeshStatus);
            }

            if (zdal2DBMeshZeroMinConnInited.compareAndSet(false, true)) {
                config.resetZdal2DBMeshMinConnStatus();
                this.zdal2DBMeshZeroMinConn = config.zdal2DBMeshZeroMinConn;
                this.globalZdal2DBMeshZeroMinConn = config.globalZdal2DBMeshZeroMinConn;
                this.dbkeyZdal2DBMeshZeroMinConnMap = config.dbkeyZdal2DBMeshZeroMinConnMap;
                this.zoneZdal2DBMeshZeroMinConnMap = config.zoneZdal2DBMeshZeroMinConnMap;
            } else {
                LOGGER
                    .warn(
                        "zdal2DBMeshZeroMinConn not support dynamic reset, current status: {}, wanted status: {}",
                        this.zdal2DBMeshZeroMinConn, config.zdal2DBMeshZeroMinConn);
            }

            if (useDBMeshTemplateInited.compareAndSet(false, true)) {
                this.useDBMeshTemplate = config.useDBMeshTemplate;
            } else {
                LOGGER
                    .warn(
                        "useDBMeshTemplate not support dynamic reset, current status: {}, wanted status: {}",
                        this.useDBMeshTemplate, config.useDBMeshTemplate);
            }

            if (useDBMeshSpecificInit.compareAndSet(false, true)) {
                this.useDBMeshSpecific = config.useDBMeshSpecific;
                this.useDBMeshDatabase = config.useDBMeshDatabase;
                this.useDBMeshUsername = config.useDBMeshUsername;
                this.useDBMeshPassword = config.useDBMeshPassword;
            }
        }
    }

    private void resetOcj2ObProxyStatus() {

        if (StringUtils.isEmpty(ocj2ObProxyStatus)) {
            globalOcj2ObProxyStatus = DsInitStatus.OFF;
            dbkeyOcj2ObProxyStatusMap = null;
            zoneOcj2ObProxyStatusMap = null;
            return;
        }

        DsInitStatus globalOcj2ObProxyTemp = DsInitStatus.OFF;
        Map<String, DsInitStatus> dsOcj2ObProxyMapTemp = new HashMap<String, DsInitStatus>();
        TreeMap<String, DsInitStatus> zoneOcj2ObProxyMapTemp = new TreeMap<String, DsInitStatus>();

        String dbmodeOption = getCurrentOptionByDbMode(ocj2ObProxyStatus);
        String[] pieces = dbmodeOption.split(";");
        for (String piece : pieces) {
            String[] configs = piece.split("#");
            if (configs.length < 2) {
                continue;
            }
            String type = configs[0].trim();
            if ("GLOBAL".equalsIgnoreCase(type)) {
                if ("DYNAMIC".equalsIgnoreCase(configs[1])) {
                    globalOcj2ObProxyTemp = DsInitStatus.DYNAMIC;
                } else if ("OBPROXY".equalsIgnoreCase(configs[1])) {
                    globalOcj2ObProxyTemp = DsInitStatus.OBPROXY;
                }
            } else if ("ZONE".equalsIgnoreCase(type)) {
                String[] zoneOcj2ObProxyConfig = configs[1].split(":");

                DsInitStatus tmp = DsInitStatus.OFF;
                if ("DYNAMIC".equalsIgnoreCase(zoneOcj2ObProxyConfig[1])) {
                    tmp = DsInitStatus.DYNAMIC;
                } else if ("OBPROXY".equalsIgnoreCase(zoneOcj2ObProxyConfig[1])) {
                    tmp = DsInitStatus.OBPROXY;
                }
                zoneOcj2ObProxyMapTemp.put(zoneOcj2ObProxyConfig[0].trim(), tmp);
            } else if ("DBKEY".equalsIgnoreCase(type)) {
                String[] dsOcj2ObProxyConfig = configs[1].split(":");
                DsInitStatus tmp = DsInitStatus.OFF;
                if ("DYNAMIC".equalsIgnoreCase(dsOcj2ObProxyConfig[1])) {
                    tmp = DsInitStatus.DYNAMIC;
                } else if ("OBPROXY".equalsIgnoreCase(dsOcj2ObProxyConfig[1])) {
                    tmp = DsInitStatus.OBPROXY;
                }
                dsOcj2ObProxyMapTemp.put(dsOcj2ObProxyConfig[0].trim(), tmp);
            }
        }

        // 对一致性要求不高，所以没有保证严格的同时生效
        globalOcj2ObProxyStatus = globalOcj2ObProxyTemp;
        dbkeyOcj2ObProxyStatusMap = dsOcj2ObProxyMapTemp;
        zoneOcj2ObProxyStatusMap = zoneOcj2ObProxyMapTemp;
    }

    private void resetZdal2DBMeshStatus() {

        if (StringUtils.isEmpty(zdal2DBMeshStatus)) {
            zdal2DBMeshStatusJudge = ZdalInitStatus.OFF;
            return;
        }

        ZdalInitStatus globalZdal2DBMeshStatusTemp = ZdalInitStatus.OFF;
        TreeMap<String, ZdalInitStatus> zoneZdal2DBMeshStatusMapTemp = new TreeMap<String, ZdalInitStatus>();

        String dbmodeOption = getCurrentOptionByDbMode(zdal2DBMeshStatus);
        String[] pieces = dbmodeOption.split(";");
        for (String piece : pieces) {
            String[] configs = piece.split("#");
            if (configs.length < 2) {
                continue;
            }
            String type = configs[0].trim();
            if ("GLOBAL".equalsIgnoreCase(type)) {
                if ("DYNAMIC".equalsIgnoreCase(configs[1])) {
                    globalZdal2DBMeshStatusTemp = ZdalInitStatus.DYNAMIC;
                } else if ("DBMESH".equalsIgnoreCase(configs[1])) {
                    globalZdal2DBMeshStatusTemp = ZdalInitStatus.DBMESH;
                }
            } else if ("ZONE".equalsIgnoreCase(type)) {
                String[] zoneZdal2DBMeshConfig = configs[1].split(":");
                ZdalInitStatus tmp = ZdalInitStatus.OFF;
                if ("DYNAMIC".equalsIgnoreCase(zoneZdal2DBMeshConfig[1])) {
                    tmp = ZdalInitStatus.DYNAMIC;
                } else if ("DBMESH".equalsIgnoreCase(zoneZdal2DBMeshConfig[1])) {
                    tmp = ZdalInitStatus.DBMESH;
                }
                zoneZdal2DBMeshStatusMapTemp.put(zoneZdal2DBMeshConfig[0].trim(), tmp);
            }
        }

        for (Map.Entry<String, ZdalInitStatus> zoneZdal2DBMeshStatusEntry : zoneZdal2DBMeshStatusMapTemp
            .descendingMap().entrySet()) {
            if (ZdalZoneUtil.isCurrentZonePrefix(zoneZdal2DBMeshStatusEntry.getKey())) {
                zdal2DBMeshStatusJudge = zoneZdal2DBMeshStatusEntry.getValue();
                return;
            }
        }

        zdal2DBMeshStatusJudge = globalZdal2DBMeshStatusTemp;
    }

    private void resetZdal2DBMeshMinConnStatus() {

        if (StringUtils.isEmpty(zdal2DBMeshZeroMinConn)) {
            globalZdal2DBMeshZeroMinConn = DsInitMinConnStatus.OFF;
            dbkeyZdal2DBMeshZeroMinConnMap = null;
            zoneZdal2DBMeshZeroMinConnMap = null;
            return;
        }

        DsInitMinConnStatus globalZdal2DBMeshMinConnStatusTemp = DsInitMinConnStatus.OFF;
        Map<String, DsInitMinConnStatus> dsZdal2DBMeshMinConnStatusMapTemp = new HashMap<String, DsInitMinConnStatus>();
        TreeMap<String, DsInitMinConnStatus> zoneZdal2DBMeshMinConnStatusMapTemp = new TreeMap<String, DsInitMinConnStatus>();

        String dbmodeOption = getCurrentOptionByDbMode(zdal2DBMeshZeroMinConn);
        String[] pieces = dbmodeOption.split(";");
        for (String piece : pieces) {
            String[] configs = piece.split("#");
            if (configs.length < 2) {
                continue;
            }
            String type = configs[0].trim();
            if ("GLOBAL".equalsIgnoreCase(type)) {
                if ("ZDAL".equalsIgnoreCase(configs[1])) {
                    globalZdal2DBMeshMinConnStatusTemp = DsInitMinConnStatus.ZDAL;
                } else if ("DBMESH".equalsIgnoreCase(configs[1])) {
                    globalZdal2DBMeshMinConnStatusTemp = DsInitMinConnStatus.DBMESH;
                }
            } else if ("ZONE".equalsIgnoreCase(type)) {
                String[] zoneZdal2DBMeshMinConnStatusConfig = configs[1].split(":");

                DsInitMinConnStatus tmp = DsInitMinConnStatus.OFF;
                if ("ZDAL".equalsIgnoreCase(zoneZdal2DBMeshMinConnStatusConfig[1])) {
                    tmp = DsInitMinConnStatus.ZDAL;
                } else if ("DBMESH".equalsIgnoreCase(zoneZdal2DBMeshMinConnStatusConfig[1])) {
                    tmp = DsInitMinConnStatus.DBMESH;
                }
                zoneZdal2DBMeshMinConnStatusMapTemp.put(
                    zoneZdal2DBMeshMinConnStatusConfig[0].trim(), tmp);
            } else if ("DBKEY".equalsIgnoreCase(type)) {
                String[] dsZdal2DBMeshMinConnStatusConfig = configs[1].split(":");
                DsInitMinConnStatus tmp = DsInitMinConnStatus.OFF;
                if ("ZDAL".equalsIgnoreCase(dsZdal2DBMeshMinConnStatusConfig[1])) {
                    tmp = DsInitMinConnStatus.ZDAL;
                } else if ("DBMESH".equalsIgnoreCase(dsZdal2DBMeshMinConnStatusConfig[1])) {
                    tmp = DsInitMinConnStatus.DBMESH;
                }
                dsZdal2DBMeshMinConnStatusMapTemp.put(dsZdal2DBMeshMinConnStatusConfig[0].trim(),
                    tmp);
            }
        }

        // 对一致性要求不高，所以没有保证严格的同时生效
        globalZdal2DBMeshZeroMinConn = globalZdal2DBMeshMinConnStatusTemp;
        dbkeyZdal2DBMeshZeroMinConnMap = dsZdal2DBMeshMinConnStatusMapTemp;
        zoneZdal2DBMeshZeroMinConnMap = zoneZdal2DBMeshMinConnStatusMapTemp;
    }

    public String getOcj2ObProxyStatus() {
        return ocj2ObProxyStatus;
    }

    public void setOcj2ObProxyStatus(String ocj2ObProxyStatus) {
        this.ocj2ObProxyStatus = ocj2ObProxyStatus;
    }

    private void resetOcj2ObProxySwitch() {
        if (StringUtils.isEmpty(ocj2ObProxySwitch)) {
            globalOcj2ObProxySwitch = false;
            dbkeyOcj2ObProxySwitchMap = null;
            zoneOcj2ObProxySwitchMap = null;
            return;
        }

        boolean globalOcj2ObProxyTemp = false;
        Map<String, Boolean> dsOcj2ObProxyMapTemp = new HashMap<String, Boolean>();
        TreeMap<String, Boolean> zoneOcj2ObProxyMapTemp = new TreeMap<String, Boolean>();

        String dbmodeOption = getCurrentOptionByDbMode(ocj2ObProxySwitch);
        String[] pieces = dbmodeOption.split(";");
        for (String piece : pieces) {
            String[] configs = piece.split("#");
            if (configs.length < 2) {
                continue;
            }
            String type = configs[0].trim();
            if ("GLOBAL".equalsIgnoreCase(type)) {
                globalOcj2ObProxyTemp = "on".equalsIgnoreCase(configs[1]);
            } else if ("ZONE".equalsIgnoreCase(type)) {
                String[] zoneOcj2ObProxyConfig = configs[1].split(":");
                zoneOcj2ObProxyMapTemp.put(zoneOcj2ObProxyConfig[0].trim(),
                    "on".equalsIgnoreCase(zoneOcj2ObProxyConfig[1]));
            } else if ("DBKEY".equalsIgnoreCase(type)) {
                String[] dsOcj2ObProxyConfig = configs[1].split(":");
                dsOcj2ObProxyMapTemp.put(dsOcj2ObProxyConfig[0].trim(),
                    "on".equalsIgnoreCase(dsOcj2ObProxyConfig[1]));
            }
        }

        // 对一致性要求不高，所以没有保证严格的同时生效
        globalOcj2ObProxySwitch = globalOcj2ObProxyTemp;
        dbkeyOcj2ObProxySwitchMap = dsOcj2ObProxyMapTemp;
        zoneOcj2ObProxySwitchMap = zoneOcj2ObProxyMapTemp;
    }

    private void resetZdal2DBmeshSwitch() {
        if (StringUtils.isEmpty(zdal2DBMeshSwitch)) {
            zdal2DBMeshSwitchJudge = false;
            return;
        }

        boolean globalZdal2DBMeshTemp = false;
        TreeMap<String, Boolean> zoneZdal2DBMeshMapTemp = new TreeMap<String, Boolean>();

        String dbmodeOption = getCurrentOptionByDbMode(zdal2DBMeshSwitch);
        String[] pieces = dbmodeOption.split(";");
        for (String piece : pieces) {
            String[] configs = piece.split("#");
            if (configs.length < 2) {
                continue;
            }
            String type = configs[0].trim();
            if ("GLOBAL".equalsIgnoreCase(type)) {
                globalZdal2DBMeshTemp = "on".equalsIgnoreCase(configs[1]);
            } else if ("ZONE".equalsIgnoreCase(type)) {
                String[] zoneZdal2DBMeshConfig = configs[1].split(":");
                zoneZdal2DBMeshMapTemp.put(zoneZdal2DBMeshConfig[0].trim(),
                    "on".equalsIgnoreCase(zoneZdal2DBMeshConfig[1]));
            }
        }

        for (Map.Entry<String, Boolean> zoneZdal2DBMeshStatusEntry : zoneZdal2DBMeshMapTemp
            .descendingMap().entrySet()) {
            if (ZdalZoneUtil.isCurrentZonePrefix(zoneZdal2DBMeshStatusEntry.getKey())) {
                zdal2DBMeshSwitchJudge = zoneZdal2DBMeshStatusEntry.getValue();
                return;
            }
        }

        zdal2DBMeshSwitchJudge = globalZdal2DBMeshTemp;
    }

    /**
     * @return 是否是最终状态。最终状态表示直接不初始化 OCJ 数据源
     */
    public boolean finalOcj2ObProxyStatus(String dsName) {
        return checkOcj2ObProxyStatus(dsName, DsInitStatus.OBPROXY);
    }

    /**
     * @return 是否是动态状态。动态状态需要初始化动态数据源
     */
    public boolean dynamicOcj2ObProxyStatus(String dsName) {
        return checkOcj2ObProxyStatus(dsName, DsInitStatus.DYNAMIC);
    }

    private boolean checkOcj2ObProxyStatus(String dsName, DsInitStatus expectStatus) {

        Boolean dsStatus = null;
        if (dbkeyOcj2ObProxyStatusMap == null || dbkeyOcj2ObProxyStatusMap.size() > 0) {
            dsStatus = false;
        }
        Map<String, DsInitStatus> dsOcj2ObProxyStatusMapTemp = dbkeyOcj2ObProxyStatusMap;
        if (StringUtils.isNotEmpty(dsName) && dsOcj2ObProxyStatusMapTemp != null
            && dsOcj2ObProxyStatusMapTemp.containsKey(dsName)) {
            DsInitStatus temp = dsOcj2ObProxyStatusMapTemp.get(dsName);
            dsStatus = expectStatus.equals(temp);
        }

        Boolean zoneStatus = null;
        if (zoneOcj2ObProxyStatusMap == null || zoneOcj2ObProxyStatusMap.size() > 0) {
            zoneStatus = false;
        }
        TreeMap<String, DsInitStatus> zoneOcj2ObProxyStatusMapTemp = zoneOcj2ObProxyStatusMap;
        if (zoneOcj2ObProxyStatusMapTemp != null) {
            for (Map.Entry<String, DsInitStatus> zoneOcj2ObProxySwitchEntry : zoneOcj2ObProxyStatusMapTemp
                .descendingMap().entrySet()) {
                if (ZdalZoneUtil.isCurrentZonePrefix(zoneOcj2ObProxySwitchEntry.getKey())) {
                    zoneStatus = expectStatus.equals(zoneOcj2ObProxySwitchEntry.getValue());
                    break;
                }
            }
        }

        if (dsStatus != null && zoneStatus != null) {
            return dsStatus && zoneStatus;
        } else if (dsStatus != null) {
            return dsStatus;
        } else if (zoneStatus != null) {
            return zoneStatus;
        }

        return expectStatus.equals(globalOcj2ObProxyStatus);
    }

    /**
     * @return 是否初始化 Zdal   最小连接数
     */
    public boolean useZdalMinConnStatus(String dsName) {
        return !checkZdal2DBMeshMinConnStatus(dsName, DsInitMinConnStatus.ZDAL);
    }

    /**
     * @return 是否初始化 DBMesh 最小连接数
     */
    public boolean useDBMeshMinConnStatus(String dsName) {
        return !checkZdal2DBMeshMinConnStatus(dsName, DsInitMinConnStatus.DBMESH);
    }

    private boolean checkZdal2DBMeshMinConnStatus(String dsName, DsInitMinConnStatus expectStatus) {

        Boolean dsStatus = null;

        Map<String, DsInitMinConnStatus> dbkeyZdal2DBMeshMinConnStatusMapTemp = dbkeyZdal2DBMeshZeroMinConnMap;

        if (StringUtils.isNotEmpty(dsName) && dbkeyZdal2DBMeshMinConnStatusMapTemp != null
            && dbkeyZdal2DBMeshMinConnStatusMapTemp.containsKey(dsName)) {
            DsInitMinConnStatus temp = dbkeyZdal2DBMeshMinConnStatusMapTemp.get(dsName);
            dsStatus = expectStatus.equals(temp);
        }

        Boolean zoneStatus = null;
        TreeMap<String, DsInitMinConnStatus> zoneZdal2DBMeshMinConnStatusMapTemp = zoneZdal2DBMeshZeroMinConnMap;
        if (zoneZdal2DBMeshMinConnStatusMapTemp != null) {
            for (Map.Entry<String, DsInitMinConnStatus> zoneZdal2DBMeshMinConnStatusMapEntry : zoneZdal2DBMeshMinConnStatusMapTemp
                .descendingMap().entrySet()) {
                if (ZdalZoneUtil.isCurrentZonePrefix(zoneZdal2DBMeshMinConnStatusMapEntry.getKey())) {
                    zoneStatus = expectStatus.equals(zoneZdal2DBMeshMinConnStatusMapEntry
                        .getValue());
                    break;
                }
            }
        }

        if (dsStatus != null && zoneStatus != null) {
            return dsStatus && zoneStatus;
        } else if (dsStatus != null) {
            return dsStatus;
        } else if (zoneStatus != null) {
            return zoneStatus;
        }

        return expectStatus.equals(globalZdal2DBMeshZeroMinConn);
    }

    /**
     * @return 是否是动态开关。如果是开的话，需要将流量切到 OBPROXY
     */
    public boolean dynamicOcj2ObProxySwitch(String dsName) {
        // 非动态状态 不生效
        if (!dynamicOcj2ObProxyStatus(dsName)) {
            return false;
        }

        Boolean dsSwitch = null;
        if (dbkeyOcj2ObProxySwitchMap == null || dbkeyOcj2ObProxySwitchMap.size() > 0) {
            dsSwitch = false;
        }
        Map<String, Boolean> dsOcj2ObProxySwitchMapTemp = dbkeyOcj2ObProxySwitchMap;
        if (StringUtils.isNotEmpty(dsName) && dsOcj2ObProxySwitchMapTemp != null) {
            if (dsOcj2ObProxySwitchMapTemp.containsKey(dsName)) {
                dsSwitch = dsOcj2ObProxySwitchMapTemp.get(dsName);
            }
        }

        Boolean zoneSwitch = null;
        if (zoneOcj2ObProxySwitchMap == null || zoneOcj2ObProxySwitchMap.size() > 0) {
            zoneSwitch = false;
        }
        TreeMap<String, Boolean> zoneOcj2ObProxySwitchMapTemp = zoneOcj2ObProxySwitchMap;
        if (zoneOcj2ObProxySwitchMapTemp != null) {
            for (Map.Entry<String, Boolean> zoneOcj2ObProxySwitchEntry : zoneOcj2ObProxySwitchMapTemp
                .descendingMap().entrySet()) {
                if (ZdalZoneUtil.isCurrentZonePrefix(zoneOcj2ObProxySwitchEntry.getKey())) {
                    zoneSwitch = zoneOcj2ObProxySwitchEntry.getValue();
                    break;
                }
            }
        }

        if (dsSwitch != null && zoneSwitch != null) {
            return dsSwitch && zoneSwitch;
        } else if (dsSwitch != null) {
            return dsSwitch;
        } else if (zoneSwitch != null) {
            return zoneSwitch;
        }

        return globalOcj2ObProxySwitch;
    }

    public String getOcj2ObProxySwitch() {
        return ocj2ObProxySwitch;
    }

    public void setOcj2ObProxySwitch(String ocj2ObProxySwitch) {
        this.ocj2ObProxySwitch = ocj2ObProxySwitch;
    }

    public String getZdal2DBMeshStatus() {
        return zdal2DBMeshStatus;
    }

    public String getZdal2DBMeshZeroMinConn() {
        return zdal2DBMeshZeroMinConn;
    }

    public ZdalInitStatus getZdal2DBMeshStatusJudge() {
        return zdal2DBMeshStatusJudge;
    }

    /**
     * @return 是否是DBMESH最终状态
     */
    public boolean isZdalInitStatusDBMesh() {
        return ZdalInitStatus.DBMESH.equals(zdal2DBMeshStatusJudge);
    }

    /**
     * @return 是否是DBMESH动态状态
     */
    public boolean isZdalInitStatusDynamic() {
        return ZdalInitStatus.DYNAMIC.equals(zdal2DBMeshStatusJudge);
    }

    /**
     * @return 是否是DBMESH关闭状态
     */
    public boolean isZdalInitStatusOff() {
        return ZdalInitStatus.OFF.equals(zdal2DBMeshStatusJudge);
    }

    public String getZdal2DBMeshSwitch() {
        return zdal2DBMeshSwitch;
    }

    public boolean isZdal2DBMeshUsingDBMesh() {
        return ZdalInitStatus.DBMESH.equals(zdal2DBMeshStatusJudge)
            || (ZdalInitStatus.DYNAMIC.equals(zdal2DBMeshStatusJudge) && zdal2DBMeshSwitchJudge);
    }

    private void resetTxCommitLatencyThreshold() {
        if (StringUtils.isEmpty(txCommitLatencyThreshold)) {
            globalLatency = 0;
            dbkeyLatencyMap = null;
            zoneLatencyMap = null;
            return;
        }

        long globalLatencyTemp = 0;
        Map<String, Long> dsLatencyMapTemp = new HashMap<String, Long>();
        TreeMap<String, Long> zoneLatencyMapTemp = new TreeMap<String, Long>();

        String dbmodeOption = getCurrentOptionByDbMode(txCommitLatencyThreshold);
        String[] pieces = dbmodeOption.split(";");
        for (String piece : pieces) {
            String[] configs = piece.split("#");
            if (configs.length < 2) {
                continue;
            }
            String type = configs[0].trim();
            if ("GLOBAL".equalsIgnoreCase(type)) {
                globalLatencyTemp = Long.valueOf(configs[1]);
            } else if ("ZONE".equalsIgnoreCase(type)) {
                String[] zoneLatencyConfig = configs[1].split(":");
                zoneLatencyMapTemp.put(zoneLatencyConfig[0].trim(),
                    Long.valueOf(zoneLatencyConfig[1]));
            } else if ("DBKEY".equalsIgnoreCase(type)) {
                String[] dsLatencyConfig = configs[1].split(":");
                dsLatencyMapTemp.put(dsLatencyConfig[0].trim(), Long.valueOf(dsLatencyConfig[1]));
            }
        }

        // 对一致性要求不高，所以没有保证严格的同时生效
        globalLatency = globalLatencyTemp;
        dbkeyLatencyMap = dsLatencyMapTemp;
        zoneLatencyMap = zoneLatencyMapTemp;
    }

    private void resetUseAutoBatchSqlSwitch() {
        if (StringUtils.isEmpty(useAutoBatchSqlSwitch)) {
            globalUseAutoBatchSql = false;
            dbkeyUseAutoBatchSqlMap = null;
            zoneUseAutoBatchSqlMap = null;
            return;
        }

        boolean globalTemp = false;
        Map<String, Boolean> dbkeyMapTemp = new HashMap<String, Boolean>();
        TreeMap<String, Boolean> zoneMapTemp = new TreeMap<String, Boolean>();

        String dbmodeOption = getCurrentOptionByDbMode(useAutoBatchSqlSwitch);
        String[] pieces = dbmodeOption.split(";");
        for (String piece : pieces) {
            String[] configs = piece.split("#");
            if (configs.length < 2) {
                continue;
            }
            String type = configs[0].trim();
            if ("GLOBAL".equalsIgnoreCase(type)) {
                globalTemp = "on".equalsIgnoreCase(configs[1]);
            } else if ("DBKEY".equalsIgnoreCase(type)) {
                String[] dsOcj2ObProxyConfig = configs[1].split(":");
                dbkeyMapTemp.put(dsOcj2ObProxyConfig[0].trim(),
                    "on".equalsIgnoreCase(dsOcj2ObProxyConfig[1]));
            } else if ("ZONE".equalsIgnoreCase(type)) {
                String[] zoneOcj2ObProxyConfig = configs[1].split(":");
                zoneMapTemp.put(zoneOcj2ObProxyConfig[0].trim(),
                    "on".equalsIgnoreCase(zoneOcj2ObProxyConfig[1]));
            }
        }

        // 对一致性要求不高，所以没有保证严格的同时生效
        globalUseAutoBatchSql = globalTemp;
        dbkeyUseAutoBatchSqlMap = dbkeyMapTemp;
        zoneUseAutoBatchSqlMap = zoneMapTemp;
    }

    String getCurrentOptionByDbMode(String originString) {
        // example: option1|option2
        String[] dbmodeOptions = originString.split("\\|");

        String defaultOption = "";
        for (String dbmodeOption : dbmodeOptions) {
            // example: DBMODE#xx1,xx2;GLOBAL#10
            String[] allConfigs = dbmodeOption.split(";");

            boolean hasDbMode = false;
            for (String allConfig : allConfigs) {
                // example: DBMODE#xx1,xx2
                String[] configs = allConfig.split("#");

                if (configs.length < 2) {
                    continue;
                }
                if ("DBMODE".equalsIgnoreCase(configs[0])) {
                    hasDbMode = true;

                    // example: xx1,xx2
                    String dbmode = configs[1];
                    String[] temps = dbmode.split(",");
                    for (String temp : temps) {
                        if (ZdalConfigUtil.isCurrentDbMode(temp)) {
                            LOGGER.warn(
                                "dynamic attribute [{}] use current dbmode [{}] option: [{}]",
                                originString, ZdalConfigUtil.currentDbMode(), dbmodeOption);
                            return dbmodeOption;
                        }
                    }
                    break;
                }
            }
            // get first default option
            if (!hasDbMode && "".equals(defaultOption)) {
                defaultOption = dbmodeOption;
            }
        }

        LOGGER.warn("dynamic attribute [{}] use default option [{}], current dbmode [{}]",
            originString, defaultOption, ZdalConfigUtil.currentDbMode());
        return defaultOption;
    }

    public long getTxCommitLatency(String dsName) {
        Long dsLatency = null;
        if (dbkeyLatencyMap == null || dbkeyLatencyMap.size() > 0) {
            dsLatency = 0L;
        }
        Map<String, Long> dsLatencyMapTemp = dbkeyLatencyMap;
        if (StringUtils.isNotEmpty(dsName) && dsLatencyMapTemp != null) {
            if (dsLatencyMapTemp.containsKey(dsName)) {
                dsLatency = dsLatencyMapTemp.get(dsName);
            }
        }

        Long zoneLatency = null;
        if (zoneLatencyMap == null || zoneLatencyMap.size() > 0) {
            zoneLatency = 0L;
        }
        TreeMap<String, Long> zoneLatencyMapTemp = zoneLatencyMap;
        if (zoneLatencyMapTemp != null) {
            for (Map.Entry<String, Long> zoneLatencyEntry : zoneLatencyMapTemp.descendingMap()
                .entrySet()) {
                if (ZdalZoneUtil.isCurrentZonePrefix(zoneLatencyEntry.getKey())) {
                    zoneLatency = zoneLatencyEntry.getValue();
                    break;
                }
            }
        }

        if (dsLatency != null && zoneLatency != null) {
            if (dsLatency == 0) {
                return dsLatency;
            } else if (zoneLatency == 0) {
                return zoneLatency;
            } else {
                return dsLatency;
            }
        } else if (dsLatency != null) {
            return dsLatency;
        } else if (zoneLatency != null) {
            return zoneLatency;
        }

        return globalLatency;
    }

    public String getTxCommitLatencyThreshold() {
        return txCommitLatencyThreshold;
    }

    public void setTxCommitLatencyThreshold(String txCommitLatencyThreshold) {
        this.txCommitLatencyThreshold = txCommitLatencyThreshold;
    }

    public boolean useAutoBatchSql(String dsName) {
        Boolean dsStatus = null;
        if (dbkeyUseAutoBatchSqlMap == null || dbkeyUseAutoBatchSqlMap.size() > 0) {
            dsStatus = false;
        }

        Map<String, Boolean> dsUseAutoBatchSqlMapTemp = dbkeyUseAutoBatchSqlMap;
        if (StringUtils.isNotEmpty(dsName) && dsUseAutoBatchSqlMapTemp != null) {
            if (dsUseAutoBatchSqlMapTemp.containsKey(dsName)) {
                dsStatus = dsUseAutoBatchSqlMapTemp.get(dsName);
            }
        }

        Boolean zoneStatus = null;
        if (zoneUseAutoBatchSqlMap == null || zoneUseAutoBatchSqlMap.size() > 0) {
            zoneStatus = false;
        }

        TreeMap<String, Boolean> zoneUseAutoBatchSqlMapTemp = zoneUseAutoBatchSqlMap;
        if (zoneUseAutoBatchSqlMapTemp != null) {
            for (Map.Entry<String, Boolean> zoneLatencyEntry : zoneUseAutoBatchSqlMapTemp
                .descendingMap().entrySet()) {
                if (ZdalZoneUtil.isCurrentZonePrefix(zoneLatencyEntry.getKey())) {
                    zoneStatus = zoneLatencyEntry.getValue();
                    break;
                }
            }
        }

        if (dsStatus != null && zoneStatus != null) {
            return dsStatus && zoneStatus;
        } else if (dsStatus != null) {
            return dsStatus;
        } else if (zoneStatus != null) {
            return zoneStatus;
        }

        return globalUseAutoBatchSql;
    }

    public String getUseAutoBatchSqlSwitch() {
        return useAutoBatchSqlSwitch;
    }

    public void setUseAutoBatchSqlSwitch(String useAutoBatchSqlSwitch) {
        this.useAutoBatchSqlSwitch = useAutoBatchSqlSwitch;
    }

    public void setZdal2DBMeshStatus(String zdal2DBMeshStatus) {
        this.zdal2DBMeshStatus = zdal2DBMeshStatus;
    }

    public void setZdal2DBMeshZeroMinConn(String zdal2DBMeshZeroMinConn) {
        this.zdal2DBMeshZeroMinConn = zdal2DBMeshZeroMinConn;
    }

    public void setZdal2DBMeshSwitch(String zdal2DBMeshSwitch) {
        this.zdal2DBMeshSwitch = zdal2DBMeshSwitch;
    }

    public boolean isUseDBMeshTemplate() {
        return useDBMeshTemplate;
    }

    public void setUseDBMeshTemplate(boolean useDBMeshTemplate) {
        this.useDBMeshTemplate = useDBMeshTemplate;
    }

    public boolean isUseDBMeshSpecific() {
        return useDBMeshSpecific;
    }

    public void setUseDBMeshSpecific(boolean useDBMeshSpecific) {
        this.useDBMeshSpecific = useDBMeshSpecific;
    }

    public boolean isUseZdalRule() {
        return useZdalRule;
    }

    public void setUseZdalRule(boolean useZdalRule) {
        this.useZdalRule = useZdalRule;
    }

    public String getUseDBMeshDatabase() {
        return useDBMeshDatabase;
    }

    public void setUseDBMeshDatabase(String useDBMeshDatabase) {
        this.useDBMeshDatabase = useDBMeshDatabase;
    }

    public String getUseDBMeshUsername() {
        return useDBMeshUsername;
    }

    public void setUseDBMeshUsername(String useDBMeshUsername) {
        this.useDBMeshUsername = useDBMeshUsername;
    }

    public String getUseDBMeshPassword() {
        return useDBMeshPassword;
    }

    public void setUseDBMeshPassword(String useDBMeshPassword) {
        this.useDBMeshPassword = useDBMeshPassword;
    }

    @Override
    public String toString() {
        return "ZdalAttributesConfig{" + "dbTrace=" + dbTrace + ", sequenceRefresh="
            + sequenceRefresh + ", sequenceRefreshInterval=" + sequenceRefreshInterval
            + ", sequenceRefreshThreshold=" + sequenceRefreshThreshold
            + ", useAutoBatchSqlSwitch=" + useAutoBatchSqlSwitch + ", sequenceLogPrintInterval="
            + sequenceLogPrintInterval + ", txCommitLatencyThreshold='"
            + txCommitLatencyThreshold + '\'' + ", ocj2ObProxyStatus='" + ocj2ObProxyStatus
            + '\'' + ", ocj2ObProxySwitch='" + ocj2ObProxySwitch + '\'' + '}';
    }

}
