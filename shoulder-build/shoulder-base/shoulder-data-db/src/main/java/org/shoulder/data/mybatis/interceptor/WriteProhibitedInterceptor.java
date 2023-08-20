package org.shoulder.data.mybatis.interceptor;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.exception.BaseRuntimeException;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.RegexpUtils;
import org.shoulder.core.util.ServletUtil;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Properties;

/**
 * 写权限控制 拦截器
 * 该拦截器常用于演示环境
 *
 * @author lym
 */
@SuppressWarnings("AlibabaUndefineMagicConstant")
@Slf4j
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class WriteProhibitedInterceptor implements Interceptor {

    private final String[] importantDataList;

    private final String[] allowIpList;

    public WriteProhibitedInterceptor() {
        importantDataList = new String[]{"Tenant", "GlobalUser", "User", "Menu", "Resource", "Role", "Dictionary", "Parameter", "Application"};
        allowIpList = new String[]{"127.0.0.1"};
    }

    public WriteProhibitedInterceptor(String[] importantDataList, String[] allowIpList) {
        this.importantDataList = importantDataList;
        this.allowIpList = allowIpList;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");

        // 读操作
        if (SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        }
        // 操作日志相关的
        if (StrUtil.containsAnyIgnoreCase(mappedStatement.getId(), importantDataList)) {
            return invocation.proceed();
        }

        // userId=1 的超级管理员 放行
        String userId = AppContext.getUserId();
        String tenant = AppContext.getTenantCode();
        log.info("mapper id={}, userId={}", mappedStatement.getId(), userId);

        //演示用的超级管理员 能查 和 增
        if (!"1".equals(userId) && (SqlCommandType.DELETE.equals(mappedStatement.getSqlCommandType()))) {
            throw new BaseRuntimeException(CommonErrorCodeEnum.PERMISSION_DENY);
        }

        //内置的租户 不能 修改、删除 权限数据
        boolean isAuthority = StrUtil.containsAnyIgnoreCase(mappedStatement.getId(), importantDataList);
        boolean write = CollectionUtil.contains(Arrays.asList(SqlCommandType.DELETE, SqlCommandType.UPDATE, SqlCommandType.INSERT), mappedStatement.getSqlCommandType());
        if ("0000".equals(tenant) && write && isAuthority) {
            // 演示环境禁止修改、删除重要数据！请登录租户【0000】:账号【shoulder】创建其他租户管理员账号后测试
            throw new BaseRuntimeException(CommonErrorCodeEnum.PERMISSION_DENY);
        }

        // ip 白名单
        if (ServletUtil.inServletContext()) {
            // 只处理可以获取到的，非请求触发 / 异步处理不能走白名单
            String ip = ServletUtil.getRemoteAddress();
            for (String allowIp : allowIpList) {
                if (RegexpUtils.matches(ip, allowIp, true)) {
                    // ip 白名单
                    return invocation.proceed();
                }
            }
        }

        //放行
        return invocation.proceed();
    }

    /**
     * 生成拦截对象的代理
     *
     * @param target     目标对象
     * @param properties mybatis配置的属性
     * @return 代理对象
     * <p>
     * mybatis配置的属性
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * mybatis配置的属性
     *
     * @param properties mybatis配置的属性
     */
    @Override
    public void setProperties(Properties properties) {

    }

}