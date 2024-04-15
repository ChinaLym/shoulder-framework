package org.shoulder.data.mybatis.interceptor.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 实体注释中生成的类型枚举
 * 角色
 *
 * @author lym
 */
@Getter
@AllArgsConstructor
public enum DataScopeType {

    /**
     * 全部
     */
    ALL,
    /**
     * 本级以及子级
     */
    ORGANIZATION_WITH_LOWER,
    /**
     * 本级
     */
    ORGANIZATION,
    /**
     * 个人
     */
    SELF,
    /**
     * 自定义
     */
    CUSTOMIZE,
    ;

    public static DataScopeType getByName(String type) {
        for (DataScopeType t : values()) {
            if (t.name().equalsIgnoreCase(type)) {
                return t;
            }
        }
        throw new IllegalArgumentException("invalid type:" + type);
    }

}
