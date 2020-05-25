package org.shoulder.log.operation.entity;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.shoulder.log.operation.constants.OpLogConstants.I18nPrefix.ACTION;

/**
 * 操作参数
 *
 * @author lym
 */
public class ActionParam {

    /**
     * 参数值是否支持多语言
     */
    protected boolean i18nValue = false;

    /**
     * 参数名称
     * 多语言key的格式为：log.action.<动作标识>.<名称标识>.displayName
     *
     *  示例：查询操作（log.action.query.displayName）的操作参数
     * [{"name":"role","type":"locale","value":"admin,operation"}]
     * name字段的多语言 key 为log.action.query.role.displayName=角色
     */
    protected String name;

    /**
     * 参数值，若存在多个值，以英文逗号","分隔，如"admin,operation"
     * 当type的值为locale时，支持多语言
     */
    protected List<String> value = new LinkedList<>();

    public ActionParam() {
    }

    public boolean isI18nValue() {
        return i18nValue;
    }

    public void setI18nValue(boolean i18nValue) {
        this.i18nValue = i18nValue;
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

    /**
     * 组装 name
     */
    private String buildName(String action){
        if(this.name.startsWith(ACTION)){
            return this.name;
        }else if(action.startsWith(ACTION)){
            return action + "." + this.name;
        }else {
            return ACTION + action + "." + this.name;
        }
    }

    private String buildValue(String value, String action) {
        if(value.startsWith(ACTION)){
            return value;
        }
        if(this.name.startsWith(ACTION)){
            return this.name + "." + value;
        }else if(action.startsWith(ACTION)){
            return action + "." + this.name + ".";
        }else {
            return ACTION + action +
                    "." + this.name +
                    "." + value;
        }
    }

    /**
     * @param action 操作动作
     */
    public String format(String action) {

        String name = buildName(action);

        StringJoiner valueJoiner = new StringJoiner(",");
        if(CollectionUtils.isNotEmpty(value)){
                value = value.stream()
                        .map( v -> {
                            if(StringUtils.isNotEmpty(v)) {
                                v = buildValue(v, action);
                            }
                            valueJoiner.add(v);
                            return v;
                        })
                        .collect(Collectors.toList());
        }
        /*else {
            throw new IllegalStateException("不应该出现的状态，要么 actionParam == null 要么 actionParam.value.size() > 0 ");
        }*/
        return "{\"name\"=\"" + name + '\"' + ", \"value\"=\"" + valueJoiner.toString() + "\"}";
    }

}
