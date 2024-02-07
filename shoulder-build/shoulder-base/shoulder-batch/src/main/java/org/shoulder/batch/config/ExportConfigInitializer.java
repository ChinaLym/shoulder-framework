package org.shoulder.batch.config;

import org.shoulder.batch.config.model.BatchInitializationSettings;
import org.shoulder.batch.config.model.ExportFileConfig;
import org.shoulder.batch.config.model.ExportLocalizeConfig;
import org.shoulder.core.context.AppInfo;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.JsonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.sql.init.AbstractScriptDatabaseInitializer;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 导出配置初始化
 *
 * @author lym
 * @see AbstractScriptDatabaseInitializer 写法参考
 */
public class ExportConfigInitializer implements ResourceLoaderAware, InitializingBean {

    private final Logger log = LoggerFactory.getLogger(ExportConfigInitializer.class);

    private final BatchInitializationSettings settings;
    private final ExportConfigManager         exportConfigManager;

    private volatile ResourcePatternResolver resourcePatternResolver;

    public ExportConfigInitializer(BatchInitializationSettings settings, ExportConfigManager exportConfigManager) {
        this.settings = settings;
        this.exportConfigManager = exportConfigManager;
    }

    @Override public void afterPropertiesSet() throws Exception {
        initializeExportConfigManager();
    }

    @Override public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }

    private void initializeExportConfigManager() {
        initializeExportConfigManagerField(settings.getExportLocalizeConfigLocations(),
            this::readResourceToExportLocalizeConfigList,
            exportConfigManager::addLocalizeConfig);

        initializeExportConfigManagerField(settings.getExportFileConfigLocations(),
            this::readResourceToExportFileConfigList,
            exportConfigManager::addFileConfig);
    }

    private <C> void initializeExportConfigManagerField(List<String> resourceLocations, Function<Resource, List<C>> resourceReader, Consumer<C> fieldSetter) {
        if(CollectionUtils.isEmpty(resourceLocations)) {
            return;
        }
        resourceLocations.stream()
            .map(this::doGetResources)
            .flatMap(List::stream)
            .map(resourceReader)
            .flatMap(List::stream)
            .forEach(fieldSetter);

        List<String> ExportFileConfigLocations;

    }

    private List<Resource> doGetResources(String location) {
        try {
            return new ArrayList<>(Arrays.asList(resourcePatternResolver.getResources(location)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to load resources from " + location, ex);
        }
    }

    private List<ExportLocalizeConfig> readResourceToExportLocalizeConfigList(Resource resource) {
        return readResource(resource, ExportLocalizeConfig.class);
    }

    private List<ExportFileConfig> readResourceToExportFileConfigList(Resource resource) {
        return readResource(resource, ExportFileConfig.class);
    }

    private <T> List<T> readResource(Resource resource, Class<T> tclass) {
        Charset charset = AppInfo.charset();
        try {
            String fileContent = resource.getContentAsString(charset);
            return JsonUtils.parseObject(fileContent, List.class, tclass);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read resource " + resource + " charset=" + charset, e);
        }
    }

}
