package org.shoulder.ext.config.domain.enums;

import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.ext.config.domain.ex.ConfigException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lym
 */
public class TenantEnum {

    /**
     * 租户
     */
    public static final String DEFAULT = "DEFAULT";


    private final String tenantName;

    private final String description;

    TenantEnum(String tenantName, String description) {
        this.tenantName = tenantName;
        this.description = description;
    }

    public static List<String> values() {
        // todo
        return new ArrayList<>();
    }

    /**
     * Gets get tenant name.
     *
     * @return the get tenant name
     */
    public String getTenantName() {
        return tenantName;
    }

    /**
     * Gets get description.
     *
     * @return the get description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the customer belongs to by the name
     *
     * @param name the name
     * @return the customer belongs to
     */
    public static String getByName(String name) {
        for (String it : values()) {
            if (it.equals(name)) {
                return it;
            }
        }
        throw new ConfigException(CommonErrorCodeEnum.TENANT_INVALID);
    }

}
