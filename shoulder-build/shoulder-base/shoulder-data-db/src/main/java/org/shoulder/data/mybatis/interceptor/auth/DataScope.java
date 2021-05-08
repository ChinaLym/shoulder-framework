package org.shoulder.data.mybatis.interceptor.auth;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;

/**
 * 数据权限
 *
 * @author lym
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataScope extends HashMap {
    /**
     * 限制范围的字段名称 （\
     * 个人外）
     */
    private String scopeName = "org_id";
    /**
     * 限制范围为个人时的字段名称
     */
    private String selfScopeName = "created_by";
    /**
     * 当前用户ID
     */
    private String userId;

    /**
     * 具体的数据范围
     */
    private List<Long> orgIds;

}
