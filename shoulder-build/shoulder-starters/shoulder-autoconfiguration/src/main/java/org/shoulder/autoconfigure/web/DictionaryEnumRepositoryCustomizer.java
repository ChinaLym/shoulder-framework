package org.shoulder.autoconfigure.web;

import org.shoulder.core.dictionary.spi.DictionaryEnumStore;

/**
 * 供使用者注册枚举类
 *
 * @author lym
 */
@FunctionalInterface
public interface DictionaryEnumRepositoryCustomizer {

    /**
     * 自定义配置，可以注册枚举类
     *
     * @param dictionaryEnumStore repo
     */
    void customize(DictionaryEnumStore dictionaryEnumStore);
}
