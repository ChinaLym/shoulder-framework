package org.shoulder.data.mybatis.interceptor.like;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;

import java.util.Map;
import java.util.Objects;

/**
 * 参数对象为Map的转换器
 *
 * @author lym
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MapLikeSqlConverter extends AbstractLikeSqlConverter<Map> {

    @Override
    public void transferWrapper(String field, Map parameter) {
        AbstractWrapper wrapper = (AbstractWrapper) parameter.get(MYBATIS_PLUS_WRAPPER_KEY);
        parameter = wrapper.getParamNameValuePairs();
        String[] keys = field.split(MYBATIS_PLUS_WRAPPER_SEPARATOR_REGEX);
        // ew.paramNameValuePairs.param1，截取字符串之后，获取第三个，即为参数名
        String paramName = keys[2];
        String mapKey = String.format("%s.%s", REPLACED_LIKE_KEYWORD_MARK, paramName);
        if (parameter.containsKey(mapKey) && Objects.equals(parameter.get(mapKey), true)) {
            return;
        }
        if (this.cascade(field)) {
            this.resolveCascadeObj(field, parameter);
        } else {
            Object param = parameter.get(paramName);
            if (this.hasEscapeChar(param)) {
                String paramStr = param.toString();
                parameter.put(keys[2], String.format("%%%s%%", this.escapeChar(paramStr.substring(1, paramStr.length() - 1))));
            }
        }
        parameter.put(mapKey, true);
    }

    @Override
    public void transferSelf(String field, Map parameter) {
        if (this.cascade(field)) {
            this.resolveCascadeObj(field, parameter);
            return;
        }
        Object param = parameter.get(field);
        if (this.hasEscapeChar(param)) {
            String paramStr = param.toString();
            parameter.put(field, String.format("%%%s%%", this.escapeChar(paramStr.substring(1, paramStr.length() - 1))));
        }
    }

    @Override
    public void transferSplice(String field, Map parameter) {
        if (this.cascade(field)) {
            this.resolveCascadeObj(field, parameter);
            return;
        }
        Object param = parameter.get(field);
        if (this.hasEscapeChar(param)) {
            parameter.put(field, this.escapeChar(param.toString()));
        }
    }

    /**
     * 处理级联属性
     *
     * @param field     级联字段名
     * @param parameter 参数Map对象
     */
    private void resolveCascadeObj(String field, Map parameter) {
        int index = field.indexOf(MYBATIS_PLUS_WRAPPER_SEPARATOR);
        Object param = parameter.get(field.substring(0, index));
        if (param == null) {
            return;
        }
        this.resolveObj(field.substring(index + 1), param);
    }

}
