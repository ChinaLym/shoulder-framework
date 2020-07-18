package org.shoulder.log.operation.entity;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.shoulder.log.operation.constants.OpLogI18nPrefix.OPERATION;

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
     * 多语言key的格式为：op.op.<动作标识>.<参数名称标识>
     *
     *  示例：查询操作（op.op.query）的操作参数
     * [{"name":"role","type":"locale","value":"admin,operation"}]
     * name字段的多语言 key 为 op.op.query.role=角色
     */
    protected String name;

    /**
     * 参数值，若存在多个值，以英文逗号","分隔，如"admin,operation"
     * 当type的值为locale时，支持多语言
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

    /**
     * 组装 name
     */
    private String buildName(String operation){
        if(this.name.startsWith(OPERATION)){
            return this.name;
        }else if(operation.startsWith(OPERATION)){
            return operation + "." + this.name;
        }else {
            return OPERATION + operation + "." + this.name;
        }
    }

    private String buildValue(String value, String operation) {
        if(value.startsWith(OPERATION)){
            return value;
        }
        if(this.name.startsWith(OPERATION)){
            return this.name + "." + value;
        }else if(operation.startsWith(OPERATION)){
            return operation + "." + this.name + ".";
        }else {
            return OPERATION + operation +
                    "." + this.name +
                    "." + value;
        }
    }

    /**
     * @param operation 操作动作
     */
    public String format(String operation) {

        String name = buildName(operation);

        StringJoiner valueJoiner = new StringJoiner(",");
        if(CollectionUtils.isNotEmpty(value)){
                value = value.stream()
                        .map( v -> {
                            if(StringUtils.isNotEmpty(v)) {
                                v = buildValue(v, operation);
                            }
                            valueJoiner.add(v);
                            return v;
                        })
                        .collect(Collectors.toList());
        }
        /*else {
            throw new IllegalStateException("不应该出现的状态，要么 operationParam == null 要么 operationParam.value.size() > 0 ");
        }*/
        return "{\"name\"=\"" + name + '\"' + ", \"value\"=\"" + valueJoiner.toString() + "\"}";
    }

}
