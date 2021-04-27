package org.shoulder.ext.config.service;

import org.shoulder.ext.config.domain.model.ConfigData;

/**
 * @author lym
 */
public interface ConfigManagerCoreService {

    /**
     * 新增
     *
     * @param configData 要新增的
     */
    void insert(ConfigData configData);

    /**
     * 更新
     *
     * @param configData 要更新的
     */
    void update(ConfigData configData);

    /**
     * 删除
     *
     * @param configData 要删除的
     * @return 删除成果
     */
    boolean delete(ConfigData configData);

    /**
     * 迁移
     *
     * @param configData the config data
     * @param overwrite  如果存在是否覆盖
     */
    void migration(ConfigData configData, boolean overwrite);
}