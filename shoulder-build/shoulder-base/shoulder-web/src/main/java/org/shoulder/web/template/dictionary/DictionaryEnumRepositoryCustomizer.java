package org.shoulder.web.template.dictionary;

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
     * @param dictionaryEnumRepository repo
     */
    void customize(DictionaryEnumRepository dictionaryEnumRepository);
}