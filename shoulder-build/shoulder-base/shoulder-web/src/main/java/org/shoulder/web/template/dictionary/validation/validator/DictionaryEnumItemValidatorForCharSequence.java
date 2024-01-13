package org.shoulder.web.template.dictionary.validation.validator;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.web.template.dictionary.model.ConfigAbleDictionaryItem;
import org.shoulder.web.template.dictionary.model.DictionaryEnum;
import org.shoulder.web.template.dictionary.model.DictionaryItem;
import org.shoulder.web.template.dictionary.spi.DictionaryEnumStore;
import org.shoulder.web.template.dictionary.validation.DictionaryEnumItem;

/**
 * @author lym
 */
public class DictionaryEnumItemValidatorForCharSequence implements ConstraintValidator<DictionaryEnumItem, CharSequence> {

    /**
     * 指定哪个字典枚举类
     */
    private Class<? extends DictionaryItem> enumClass;

    /**
     * 配置类字典使用
     */
    private String dictionaryType;

    /**
     * 允许的值不，默认为空，表示都允许，
     */
    private String[] allowCodes;

    /**
     * 不允许的值，默认为空，表示都允许，优先级大于 {@link #allowCodes}
     */
    private String[] forbiddenCodes;

    // DictionaryItemService
    private DictionaryEnumStore dictionaryEnumStore;

    @Override
    public void initialize(DictionaryEnumItem annotation) {
        enumClass = annotation.value();
        allowCodes = annotation.allowCodes();
        forbiddenCodes = annotation.forbiddenCodes();
        dictionaryType = annotation.dictionaryType();
        dictionaryEnumStore = ContextUtils.getBean(DictionaryEnumStore.class);
    }

    /**
     * Checks that the trimmed string is not empty.
     *
     * @param charSequence               The dictionaryItemDTO.code
     * @param constraintValidatorContext context in which the constraint is evaluated.
     * @return Returns <code>true</code> if the string is <code>null</code> or the length of <code>charSequence</code> between the
     * specified
     * <code>min</code> and <code>max</code> values (inclusive), <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked,rawtypes")
    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        if (charSequence == null || StringUtils.isBlank(charSequence)) {
            return true;
        }
        String code = String.valueOf(charSequence);
        DictionaryItem dictionaryItem = null;

        if (enumClass == ConfigAbleDictionaryItem.class) {
            // 动态枚举
            AssertUtils.notEquals(dictionaryType, ConfigAbleDictionaryItem.INVALID_TYPE, CommonErrorCodeEnum.ILLEGAL_PARAM);
            // 判断配置表中是否存在该字典项
//            dictionaryItem = configBizService.queryByDictionaryTypeAndCode( example);
        } else {
            // 判断该字典是否存在
            AssertUtils.isTrue(dictionaryEnumStore.contains(enumClass), CommonErrorCodeEnum.ILLEGAL_PARAM);
            // 判断该字典项是否存在
            dictionaryItem = (DictionaryItem) DictionaryEnum.fromId((Class) enumClass, code);
        }
        AssertUtils.notNull(dictionaryItem, CommonErrorCodeEnum.ILLEGAL_PARAM);

        // 黑名单
        if (forbiddenCodes.length > 0) {
            for (String forbiddenCode : forbiddenCodes) {
                if (dictionaryItem.getItemId().equals(forbiddenCode)) {
                    return false;
                }
            }
        }

        // 白名单
        if (allowCodes.length > 0) {
            for (String allowCode : allowCodes) {
                if (dictionaryItem.getItemId().equals(allowCode)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
