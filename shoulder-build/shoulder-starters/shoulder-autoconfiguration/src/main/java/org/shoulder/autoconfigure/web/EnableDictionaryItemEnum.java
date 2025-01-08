package org.shoulder.autoconfigure.web;

import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 指定 Shoulder 对哪些枚举类字典生成查询接口
 * （可方便前端开发下拉选择输入框、模糊搜索等）
 *
 * @author lym
 * @see DictionaryItemEnum
 * @see DictionaryEnumPackageScanRegistrar
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({DictionaryEnumPackageScanRegistrar.class})
@Documented
public @interface EnableDictionaryItemEnum {

    /**
     * 将自动扫描指定包路径下的 {@link org.shoulder.core.dictionary.model.DictionaryItemEnum}
     *
     * @return 要激活的包路径
     */
    String[] value();

}