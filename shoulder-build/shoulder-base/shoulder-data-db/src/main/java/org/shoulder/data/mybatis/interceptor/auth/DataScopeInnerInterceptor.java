package org.shoulder.data.mybatis.interceptor.auth;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.shoulder.core.context.AppContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 数据权限拦截器
 * <p>
 * 1，全部：不限制
 * 2，本级以及子级：只允许操作属于当前用户的 orgId 下的数据
 * 3，本级：只允许操作与当前用户的 orgId 相同的数据
 * 4，个人：自己创建的的数据
 * 5，自定义
 *
 * @author lym
 */
@Slf4j
@AllArgsConstructor
public class DataScopeInnerInterceptor implements InnerInterceptor {

    private final Function<String, Map<String, Object>> function;


    /**
     * 查找参数是否包括DataScope对象
     *
     * @param parameterObj 参数列表
     * @return DataScope
     */
    protected Optional<DataScope> findDataScope(Object parameterObj) {
        if (parameterObj == null) {
            return Optional.empty();
        }
        if (parameterObj instanceof DataScope) {
            return Optional.of((DataScope) parameterObj);
        } else if (parameterObj instanceof Map) {
            for (Object val : ((Map<?, ?>) parameterObj).values()) {
                if (val instanceof DataScope) {
                    return Optional.of((DataScope) val);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        DataScope dataScope = findDataScope(parameter).orElse(null);
        if (dataScope == null) {
            return;
        }

        String originalSql = boundSql.getSql();

        String scopeName = dataScope.getScopeName();
        String selfScopeName = dataScope.getSelfScopeName();
        String userId = dataScope.getUserId() == null ? AppContext.getUserId() : dataScope.getUserId();
        List<Long> orgIds = dataScope.getOrgIds();
        DataScopeType dsType = DataScopeType.SELF;
        if (CollectionUtil.isEmpty(orgIds)) {
            //查询当前用户的 角色 最小权限
            //userId

            //dsType orgIds
            Map<String, Object> result = function.apply(userId);
            if (result == null) {
                return;
            }

            String type = (String) result.get("dsType");
            dsType = DataScopeType.getByName(type);
            orgIds = (List<Long>) result.get("orgIds");
        }

        //查全部
        if (DataScopeType.ALL == dsType) {
            return;
        }
        //查个人
        if (DataScopeType.SELF == dsType) {
            originalSql = "select * from (" + originalSql + ") temp_data_scope where temp_data_scope." + selfScopeName + " = " + userId;
        }
        //查其他
        else if (StrUtil.isNotBlank(scopeName) && CollUtil.isNotEmpty(orgIds)) {
            String join = CollectionUtil.join(orgIds, ",");
            originalSql = "select * from (" + originalSql + ") temp_data_scope where temp_data_scope." + scopeName + " in (" + join + ")";
        }

        PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
        mpBoundSql.sql(originalSql);
    }

}
