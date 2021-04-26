package org.shoulder.ext.config.service;

import org.shoulder.ext.config.domain.model.ConfigData;

/**
 * @author lym
 */
public interface ConfigManagerCoreService {

    /**
     * Create.
     *
     * @param configData the config data
     */
    void insert(ConfigData configData);

    /**
     * Update by id.
     *
     * @param configData the config data
     */
    void update(ConfigData configData);

    /**
     * Delete by biz id and version int.
     *
     * @param configData the config data
     * @return the int
     */
    boolean delete(ConfigData configData);

    /**
     * Migration.
     *
     * @param configData the config data
     * @param overwrite
     */
    void migration(ConfigData configData, boolean overwrite);
}