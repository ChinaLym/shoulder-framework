package com.org.shoulder.apidoc.swagger.v3.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.shoulder.core.context.AppInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Response;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;

/**
 * swagger3 config
 *
 * @author lym
 */
@Configuration
@EnableOpenApi
@EnableKnife4j
public class Swagger3Config {

    /**
     * swagger3的配置文件
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
            .apiInfo(apiInfo())
            .select()
            //.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
            .apis(RequestHandlerSelectors.basePackage("**.controller"))
            .paths(PathSelectors.any())
            .build()
            //.globalRequestParameters(getGlobalRequestParameters())
            .globalResponses(HttpMethod.GET, getGlobalResponseMessage())
            .globalResponses(HttpMethod.POST, getGlobalResponseMessage())
            .globalResponses(HttpMethod.DELETE, getGlobalResponseMessage())
            .globalResponses(HttpMethod.PUT, getGlobalResponseMessage());
    }

    /**
     * 构建 api文档的详细信息函数,注意这里的注解引用的是哪个
     */
    private ApiInfo apiInfo() {
        // 获取工程名称
        String projectName = AppInfo.appId();
        return new ApiInfoBuilder()
            .title(projectName.substring(projectName.lastIndexOf("\\") + 1) + " API接口文档")
            .contact(new Contact("lym", "https://github.com/ChinaLym/shoulder-framework", "cn_lym@foxmail.com"))
            .version(AppInfo.version())
            .description("API文档")
            .build();
    }

    /**
     * 生成全局通用参数
     *
     * @return
     */
    /*private List<RequestParameter> getGlobalRequestParameters() {
        List<RequestParameter> parameters = new ArrayList<>();
        parameters.add(new RequestParameterBuilder()
                .name("x-access-token")
                .description("令牌")
                .required(false)
                .in(ParameterType.HEADER)
                .build());
        parameters.add(new RequestParameterBuilder()
                .name("Equipment-Type")
                .description("产品类型")
                .required(false)
                .in(ParameterType.HEADER)
                .build());
        return parameters;
    }*/

    /**
     * 生成通用响应信息
     *
     * @return
     */
    private List<Response> getGlobalResponseMessage() {
        List<Response> responseList = new ArrayList<>();
        responseList.add(new ResponseBuilder().code("404").description("找不到资源").build());
        return responseList;
    }

}
