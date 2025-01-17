package org.shoulder.web.template.dictionary.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import org.shoulder.core.dictionary.model.DictionaryItem;
import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.web.template.dictionary.validation.validator.DictionaryEnumItemValidatorForCharSequence;
import org.shoulder.web.template.dictionary.validation.validator.DictionaryEnumItemValidatorForDTO;
import org.shoulder.web.template.dictionary.validation.validator.DictionaryEnumItemValidatorForDTOCollection;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * JSR 305 校验器注解，标注为合法枚举，并校验枚举合法性
 *
 * @author lym
 */
@Documented
@Constraint(validatedBy = {DictionaryEnumItemValidatorForCharSequence.class, DictionaryEnumItemValidatorForDTO.class, DictionaryEnumItemValidatorForDTOCollection.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@ReportAsSingleViolation
public @interface DictionaryEnumItem {

    /**
     * 指定哪个字典枚举类
     * 枚举字典：填枚举类.class
     * 动态字典：填 ConfigAbleDictionaryItem.class
     */
    Class<? extends DictionaryItem> value();

    /**
     * 指定字典类型(配置类字典)
     * 当且仅当 value 不为枚举类时 {@link DictionaryItemEnum} 才使用该字段
     */
    String dictionaryType() default INVALID_TYPE;

    /**
     * 允许的值，默认为空，表示都允许，
     */
    String[] allowCodes() default {};

    /**
     * 不允许的值，默认为空，表示都允许，优先级大于 {@link #allowCodes}
     */
    String[] forbiddenCodes() default {};

    String message() default "not a valid enum";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 主要用于分组校验，不同分组展示不同提示信息
     * Defines several {@code @EnumValue} annotations on the same element.
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
    @Retention(RUNTIME)
    @Documented @interface List {
        DictionaryEnumItem[] value();
    }

    String INVALID_TYPE = "JUST_INVALID";
}
