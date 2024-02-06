package org.shoulder.autoconfigure.db.mybatis;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusInnerInterceptorAutoConfiguration;
import com.baomidou.mybatisplus.core.MybatisPlusVersion;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.guid.LongGuidGenerator;
import org.shoulder.core.guid.StringGuidGenerator;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.data.mybatis.config.handler.ModelMetaObjectHandler;
import org.shoulder.data.mybatis.injector.ShoulderSqlInjector;
import org.shoulder.data.mybatis.interceptor.WriteProhibitedInterceptor;
import org.shoulder.data.mybatis.interceptor.typehandler.FullLikeTypeHandler;
import org.shoulder.data.mybatis.interceptor.typehandler.LeftLikeTypeHandler;
import org.shoulder.data.mybatis.interceptor.typehandler.RightLikeTypeHandler;
import org.shoulder.data.uid.BizIdGenerator;
import org.shoulder.data.uid.DefaultEntityIdGenerator;
import org.shoulder.data.uid.EntityIdGenerator;
import org.shoulder.data.uid.KeyFieldsBizIdGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.StringJoiner;

/**
 * MybatisPlusConfig
 *
 * @author lym
 */
@AutoConfiguration(before = MybatisPlusInnerInterceptorAutoConfiguration.class)
@ConditionalOnClass(MybatisPlusVersion.class)
@EnableConfigurationProperties(DatabaseProperties.class)
public class MybatisPlusAutoConfiguration {

    protected final DatabaseProperties databaseProperties;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public MybatisPlusAutoConfiguration(DatabaseProperties databaseProperties) {
        this.databaseProperties = databaseProperties;
    }

    /**
     * 注意:
     * InnerPlugin 插件使用时需要注意顺序关系,建议使用如下顺序
     * 多租户插件,动态表名插件
     * 分页插件,乐观锁插件
     * sql性能规范插件,防止全表更新与删除插件
     * 总结: 对sql进行单次改造的优先放入,不对sql进行改造的最后放入
     * <p>
     * 参考：
     * https://mybatis.plus/guide/interceptor.html#%E4%BD%BF%E7%94%A8%E6%96%B9%E5%BC%8F-%E4%BB%A5%E5%88%86%E9%A1%B5%E6%8F%92%E4%BB%B6%E4%B8%BE%E4%BE%8B
     */
    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(List<InnerInterceptor> innerInterceptors) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        StringJoiner interceptorNames = new StringJoiner(",");
        innerInterceptors.stream().map(i -> i.getClass().getSimpleName()).forEach(interceptorNames::add);
        log.debug("Enable MybatisPlusInterceptors: {}", interceptorNames);
        if (!innerInterceptors.isEmpty()) {
            innerInterceptors.forEach(interceptor::addInnerInterceptor);
        }
        return interceptor;
    }

    @Bean
    @Order(0)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "tenantMode", havingValue = "COLUMN")
    public TenantLineInnerInterceptor tenantLineInnerInterceptor() {
        return new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public String getTenantIdColumn() {
                return databaseProperties.getTenantIdColumn();
            }

            @Override
            public boolean ignoreTable(String tableName) {
                return false;
            }

            @Override
            public Expression getTenantId() {
                return new StringValue(AppContext.getTenantCode());
            }
        });
    }

    /**
     * todo 【安全|扩展】考虑继承 PaginationInnerInterceptor 处理分页查询页码溢出，风控可能认为不是系统本身发出的请求，而是外部伪造，需要记录本次请求的ip等端信息
     */
    @Bean
    @Order(20)
    @ConditionalOnMissingBean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor();
        // 单页分页条数限制 默认 500 条，-1 不受限制，这里从配置中拿
        paginationInterceptor.setMaxLimit(databaseProperties.getLimit());
        // 数据库类型
        paginationInterceptor.setDbType(databaseProperties.getDbType());
        // 溢出总页数后是否进行处理；若查询目标页码大于总页码，true 查询第一页 false 不处理
        paginationInterceptor.setOverflow(false);
        // 开启 count 的 join 优化,只针对部分 left join https://github.com/baomidou/mybatis-plus/issues/2492
        paginationInterceptor.setOptimizeJoin(true);
        return paginationInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "optimisticLocker", havingValue = "true", matchIfMissing = true)
    public OptimisticLockerInnerInterceptor optimizationInnerInterceptor() {
        return new OptimisticLockerInnerInterceptor();
    }

    @Bean
    @Order(80)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "blockWriteFullTable", havingValue = "true")
    public BlockAttackInnerInterceptor blockWriteFullTable() {
        //防止全表更新与删除插件
        return new BlockAttackInnerInterceptor();
    }

    /**
     * 必须使用到索引，包含left join连接字段，符合索引最左原则
     * 如果因为动态SQL，bug导致update的where条件没有带上，全表更新上万条数据
     * 如果检查到使用了索引，SQL性能基本不会太差
     * SQL尽量单表执行，有查询left join的语句，必须在注释里面允许该SQL运行，否则会被拦截，有left join的语句，如果不能拆成单表执行的SQL，请leader商量在做，SQL尽量单表执行的好处：
     * 查询条件简单、易于开理解和维护
     * 扩展性极强；（可为分库分表做准备）
     * 缓存利用率高
     * where条件为空
     * where条件使用了 !=
     * where条件使用了 not 关键字
     * where条件使用了 or 关键字
     * where条件使用了 使用子查询
     */
    @Bean
    @Order(100)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "checkSqlPerformance", havingValue = "true")
    public IllegalSQLInnerInterceptor illegalSQLInnerInterceptor() {
        // sql性能规范插件
        return new IllegalSQLInnerInterceptor();
    }

    /**
     * 演示环境权限拦截器
     */
    @Bean
    @Order(15)
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = DatabaseProperties.PREFIX, name = "forbiddenWrite", havingValue = "true")
    public WriteProhibitedInterceptor writeProhibitedInterceptor() {
        return new WriteProhibitedInterceptor();
    }


    // ***********************************************************


    /**
     * 自动填充基础字段
     */
    @Bean
    @ConditionalOnMissingBean
    public ModelMetaObjectHandler modelMetaObjectHandler() {
        ModelMetaObjectHandler metaObjectHandler = new ModelMetaObjectHandler();
        log.info("ModelMetaObjectHandler [{}]", metaObjectHandler);
        return metaObjectHandler;
    }

    @Bean
    @ConditionalOnMissingBean
    public EntityIdGenerator uidGenerator(LongGuidGenerator longGuidGenerator,
                                          StringGuidGenerator stringGuidGenerator) {
        // 没 bean 可注入会提前报错，避免 NPE
        return new DefaultEntityIdGenerator(longGuidGenerator, stringGuidGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public BizIdGenerator bizIdGenerator() {
        return new KeyFieldsBizIdGenerator();
    }

    /**
     * Mybatis 自定义的类型处理器： 处理XML中  #{name,typeHandler=fullLike}
     */
    @Bean
    public LeftLikeTypeHandler getLeftLikeTypeHandler() {
        return new LeftLikeTypeHandler();
    }

    @Bean
    public RightLikeTypeHandler getRightLikeTypeHandler() {
        return new RightLikeTypeHandler();
    }

    @Bean
    public FullLikeTypeHandler getFullLikeTypeHandler() {
        return new FullLikeTypeHandler();
    }


    /**
     * mapper 额外方法
     */
    @Bean
    @ConditionalOnMissingBean
    public ShoulderSqlInjector getMySqlInjector() {
        return new ShoulderSqlInjector();
    }

}
