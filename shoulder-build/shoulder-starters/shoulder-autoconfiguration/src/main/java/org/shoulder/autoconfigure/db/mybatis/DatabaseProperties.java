package org.shoulder.autoconfigure.db.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.shoulder.autoconfigure.core.BaseAppProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数据库配置
 *
 * @author lym
 */
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = DatabaseProperties.PREFIX)
public class DatabaseProperties {

    public static final String PREFIX = BaseAppProperties.KEY_PREFIX + "db";

    /**
     * 是否启用 防止全表更新与删除插件
     */
    private Boolean blockWriteFullTable = false;

    /**
     * 是否启用  sql性能规范插件
     * 不建议生产启用，较影响性能
     */
    private Boolean checkSqlPerformance = false;

    /**
     * 分页大小限制（默认500 与 mybatis-plus 默认值一致）
     */
    private long limit = 500;

    /**
     * 数据库类型
     */
    private DbType dbType = DbType.MYSQL;

    /**
     * 是否禁止写入
     */
    private Boolean forbiddenWrite = false;

    /**
     * 事务超时时间
     */
    private int txTimeout = 60 * 60;

    /**
     * 租户库 前缀
     */
    private String tenantDatabasePrefix = "shoulder_";

    /**
     * 多租户模式
     */
    private TenantMode tenantMode = TenantMode.NONE;

    /**
     * 租户id 列名
     */
    private String tenantIdColumn = "tenant_code";

    /**
     * id 类型
     */
    private UidType uidType = UidType.SHOULDER_UID;

    /**
     * shoulderUid
     */
    private ShoulderUid shoulderUid = new ShoulderUid();

    /**
     * 统一管理事务的方法名
     */
    private List<String> transactionAttributeList = new ArrayList<>(Arrays.asList("add*", "save*", "insert*",
            "create*", "update*", "edit*", "upload*", "delete*", "remove*",
            "clean*", "recycle*", "batch*", "mark*", "disable*", "enable*", "handle*", "syn*",
            "reg*", "gen*", "*Tx"
    ));

    @Data
    public static class ShoulderUid {

        /**
         * 当前时间，相对于时间基点"${epochStr}"的增量值，单位：秒，
         * 28: 大概可以使用 8.7年, 28位即最大表示2^28的数值的秒数
         * 30: 大概可以使用 34年, 30位即最大表示2^30的数值的秒数
         * 31: 大概可以使用 68年, 31位即最大表示2^31的数值的秒数
         */
        private int timeBits = 31;
        /**
         * 机器id，
         * <p>
         * 20：100W次重启
         * 22: 最多可支持约420w次机器启动。内置实现为在启动时由数据库分配。420w = 2^22
         * 23：800w次重启  12次/天
         * 27: 1.3亿次重启 24*12次/天
         */
        private int workerBits = 23;
        /**
         * 每秒下的并发序列，13 bits可支持每秒8192个并发，即2^13个并发
         * 9: 512 并发
         * 13: 8192 并发
         */
        private int seqBits = 9;
        /**
         * Customer epoch, unit as second. For example 2016-05-20 (ms: 1463673600000)
         * 可以改成你的项目开始开始的时间
         */
        private String epochStr = "2020-09-15";
    }

    public enum UidType {
        /**
         * 类 snowflake 算法，默认使用 shoulder 的
         */
        SHOULDER_UID,
        /**
         * 通过数据库存储
         */
        DB_SEQUENCE,
        ;

    }

    /**
     * 多租户模式
     */
    public enum TenantMode {
        /**
         * 无租户；大多数系统不需要租户，只有提供基础服务的才会需要租户的概念来实现隔离
         */
        NONE,
        /**
         * 字段区分：每个表包含 租户标识 字段；弱隔离，租户往往是一个组织内部多个子组织
         */
        COLUMN,
        /**
         * 独立schema模式（mysql里相当于独立库，数据分离）；一般不会做跨租户的查询、数据统计、数据分析
         */
        SCHEMA,
        /**
         * 独立数据源模式，数据完全隔离
         */
        DATASOURCE,
        ;

        TenantMode() {
        }

        public TenantMode findByName(String type) {
            for (TenantMode t : TenantMode.values()) {
                if (t.name().equalsIgnoreCase(type)) {
                    return t;
                }
            }
            return null;
        }

    }

}
