package org.shoulder.ext.config.provider.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author lym
 */
public class TenantInfoDTO implements Serializable {

    @Serial private static final long serialVersionUID = 1947695686908521732L;
    /**
     * 英文名称
     */
    private String name;

    /**
     * 中文名称
     */
    private String displayName;

    public TenantInfoDTO() {
    }

    public TenantInfoDTO(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>displayName</tt>.
     *
     * @return property value of displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter method for property <tt>displayName</tt>.
     *
     * @param displayName value to be assigned to property displayName
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @see Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
