package org.shoulder.data.mybatis.interceptor.tenants;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.shoulder.core.context.AppContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 多租户 - SCHEMA 模式插件
 *
 * @author lym
 */
@Slf4j
public class SchemaModeInterceptor implements InnerInterceptor {

    private final String tenantDatabasePrefix;

    public SchemaModeInterceptor(String tenantDatabasePrefix) {
        this.tenantDatabasePrefix = tenantDatabasePrefix;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 统一交给 beforePrepare 处理,防止某些sql解析不到，又被beforePrepare重复处理
    }


    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        PluginUtils.MPStatementHandler mpSh = PluginUtils.mpStatementHandler(sh);
        MappedStatement ms = mpSh.mappedStatement();
        SqlCommandType sct = ms.getSqlCommandType();
        if (sct == SqlCommandType.INSERT || sct == SqlCommandType.UPDATE || sct == SqlCommandType.DELETE || sct == SqlCommandType.SELECT) {
            if (InterceptorIgnoreHelper.willIgnoreDynamicTableName(ms.getId())) {
                return;
            }
            PluginUtils.MPBoundSql mpBs = mpSh.mPBoundSql();
            log.debug("origin sql: {}", mpBs.sql());
            mpBs.sql(this.changeSchema(mpBs.sql()));
        }
    }

    protected String changeSchema(String sql) {
        // todo 若想执行sql时，不切换到 {tenantDatabasePrefix}_{TENANT} 库, 添加注解
        String tenantCode = AppContext.getTenantCode();
        if (StrUtil.isEmpty(tenantCode)) {
            return sql;
        }

        String schemaName = StrUtil.format("{}_{}", tenantDatabasePrefix, tenantCode);
        return DruidMultiSchemaUtil.changeSchema(sql, schemaName);
    }

}
