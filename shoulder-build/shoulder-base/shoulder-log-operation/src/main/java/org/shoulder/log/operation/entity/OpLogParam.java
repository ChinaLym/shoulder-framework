package org.shoulder.log.operation.entity;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 操作对应业务方法/接口的入参
 * @author lym
 */
public class OpLogParam {

    /**
     * 参数值是否支持多语言
     */
    protected boolean supportI18n = false;

    /**
     * 参数名称
     */
    protected String name;

    /**
     * 参数值
     */
    protected List<String> value = new LinkedList<>();

    public OpLogParam() {
    }

    public boolean isSupportI18n() {
        return supportI18n;
    }

    public void setSupportI18n(boolean supportI18n) {
        this.supportI18n = supportI18n;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public void addValue(String value) {
        if(this.value == null){
            this.value = new LinkedList<>();
        }
        this.value.add(value);
    }

}
