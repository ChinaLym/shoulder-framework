package org.shoulder.autoconfigure.apidoc;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.shoulder.autoconfigure.apidoc.util.RequestHandlerSelectors;
import org.shoulder.core.context.AppInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 访问
 * 在浏览器输入地址：http://host:port/doc.html
 * <p>
 * ApiDoc 相关配置
 *
 * @author lym
 */
@Configuration(proxyBeanMethods = false)
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
@ConditionalOnClass(value = {Docket.class})
@ConditionalOnProperty(value = "shoulder.apidoc.default.enable", havingValue = "true", matchIfMissing = true)
public class SwaggerAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(EnableKnife4j.class)
    @EnableKnife4j
    public static class EnableKnife4jEnhance {

    }

    /**
     * swagger3的配置文件
     */
    @Bean
    @ConditionalOnMissingBean(value = Docket.class, name = "defaultDocket")
    public Docket defaultDocket(ApiInfo apiInfo) {
        return new Docket(DocumentationType.OAS_30)
            .apiInfo(apiInfo)
            .groupName("default")
            .select()
            // 包路径中带有 controller
            .apis(RequestHandlerSelectors.packageRegex("^.*?\\.controller\\..*?$"))
            .paths(PathSelectors.any())
            .build();
    }

    /**
     * 构建 api文档的详细信息函数,注意这里的注解引用的是哪个
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiInfo apiInfo() {
        // 获取工程名称
        String projectName = AppInfo.appId();
        return new ApiInfoBuilder()
            .title(projectName + " API 接口文档")
            .contact(new Contact("lym", "https://github.com/ChinaLym/shoulder-framework", "cn_lym@foxmail.com"))
            .version(AppInfo.version())
            .description("默认 api 分组")
            .build();
    }

}
