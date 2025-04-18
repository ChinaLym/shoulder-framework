package org.shoulder.autoconfigure.batch;

import org.shoulder.batch.progress.BatchActivityRepository;

/**
 * 供使用者注册枚举类
 *
 * @author lym
 */
@FunctionalInterface
public interface BatchActivityEnumRepositoryCustomizer {

    /**
     * 自定义配置，可以注册枚举类
     *
     * @param dictionaryEnumStore repo
     */
    void customize(BatchActivityRepository repository);
}
