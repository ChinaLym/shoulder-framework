package org.shoulder.web.template.dictionary.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.shoulder.core.util.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.Serializable;

/**
 * @author lym
 */
@Getter
@Setter
public class ConfigAbleDictionaryItem implements DictionaryItem<String>, Serializable {

    private static final SpelExpressionParser EXPRESS_PARSER = new SpelExpressionParser();
    private static final long serialVersionUID = -1;
    public static final String INVALID_TYPE = "JUST_INVALID";

    @NotNull
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "\\w+")
    //@ConfigField(chineseName = "字典 bizType", indexKey = true, description = "用于标识字典类型")
    private String dictionaryType;

    // todo parentCode fixme 关联的代码

    @NotNull
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "\\w+")
    //@ConfigField(chineseName = "字典 code", indexKey = true, description = "存在数据库的值（压缩后的）")
    private String code;

    @NotNull
    @NotBlank
    @Size(max = 64)
    @Pattern(regexp = "\\w+")
    //@ConfigField(chineseName = "字典 name", indexKey = true, description = "由于代码搜索的值")
    private String name;

    @NotNull
    @NotBlank
    @Size(max = 64)
    //@ConfigField(chineseName = "展示名称", description = "在前端页面展示的值")
    private String displayName;

    @NotNull
    //@ConfigField(chineseName = "展示顺序", description = "用于下拉框排序", defaultValue = "0")
    private Integer displayOrder;

    @NotNull
    @Size(max = 512)
    //@ConfigField(chineseName = "spel过滤表达式", description = "用于条件搜索过滤，表达式需要支持key,value两个前端传入的特殊过滤条件，返回boolean类型."
    //        + "例如：币种枚举，业务上有个根据产品来搜索支持的币种，某个只支持GOL的币种表达式可以为 #key == 'product' and {'AUTO DEBIT','CASHIER'}.contains(#value)")
    private String spelFilterExpression;

    @Size(max = 1024)
    //@ConfigField(chineseName = "说明", description = "注释信息"
    private String description;

    @Override
    public String getItemId() {
        return code;
    }

    @Override
    public String getName() {
        return code;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    @Override
    public String getDictionaryType() {
        return dictionaryType;
    }

    @Override
    public boolean matchCondition(String key, String value) {
        if (StringUtils.isBlank(spelFilterExpression)) {
            return true;
        }
        try {
            Expression expression = EXPRESS_PARSER.parseExpression(spelFilterExpression);
            EvaluationContext context = new StandardEvaluationContext();
            context.setVariable("key", key);
            context.setVariable("value", value);
            return Boolean.TRUE.equals(expression.getValue(context, Boolean.class));
        } catch (Exception ignore) {
            return true;
        }
    }

    public static ConfigAbleDictionaryItem of(DictionaryItem source) {
        ConfigAbleDictionaryItem targetModel = new ConfigAbleDictionaryItem();
        targetModel.setCode(source.getItemId().toString());
        // 为空或者为0，都代表一级节点
        //        targetModel.setParentCode(StringUtils.isBlank(source.getParentCode()) ? Constants.ZERO : source
        //        .getParentCode());
        targetModel.setDictionaryType(source.getDictionaryType());
        targetModel.setName(targetModel.getName());
        targetModel.setDisplayName(source.getDisplayName());
        targetModel.setDisplayOrder(source.getDisplayOrder());
        targetModel.setDescription(source.getDescription());
        return targetModel;
    }

}
