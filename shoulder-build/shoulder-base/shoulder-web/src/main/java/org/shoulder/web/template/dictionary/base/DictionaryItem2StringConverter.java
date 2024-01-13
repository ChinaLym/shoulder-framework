/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2019 All Rights Reserved.
 */
package org.shoulder.web.template.dictionary.base;

import org.shoulder.web.template.dictionary.model.DictionaryItem;
import org.springframework.core.convert.converter.Converter;

import javax.annotation.Nullable;

/**
 * 模型转换器
 *
 * @author lym
 */
@SuppressWarnings("unchecked")
public class DictionaryItem2StringConverter implements Converter<DictionaryItem<String>, String> {

    /**
     * str 转换 enum
     *
     * @param dictionaryItem 源模型
     * @return 目标
     */
    @Override
    public String convert(@Nullable DictionaryItem<String> dictionaryItem) {
        if (dictionaryItem == null) {
            return null;
        }
        return dictionaryItem.getItemId();
    }

}
