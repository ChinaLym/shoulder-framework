package com.example.demo1.config;

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
 * <p>
 * 查看swagger-ui界面 http://127.0.0.1:8080/swagger-ui/index.html
 * http://localhost:8080/doc.html
 * <p>
 * 利用注解配置 https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations#quick-annotation-overview
 *
 * @author lym
 */
/*@OpenAPIDefinition(
        // openapi定义描述
        info = @Info(
                title = "${spring.application.name}",
                version = "1.0.0",
                description = "OpenApi3.0",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")
        ),
        // 请求服务地址配置，可以按不同的环境配置
        servers = {
                @Server(
                        url = "http://localhost:8181",
                        description = "本地地址"
                ),
                @Server(
                        url = "http://dev1-api.kevin.com",
                        description = "dev1环境地址"
                ),
                @Server(
                        url = "http://test1-api.kevin.com",
                        description = "test1环境地址"
                ),
                @Server(
                        url = "http://api.kevin.com",
                        description = "生产环境地址"
                )
        },
        // 这个tags可以用来定义一些公共参数说明，比如：token或者其他自定义key
        tags = {
                @Tag(name = "Header：" + HttpHeaders.AUTHORIZATION, description = "登录之后获取的JWT Token，类型是bearer"),
                @Tag(name = "Header：Accept-Language", description = "国际化语言：zh-CN（中文），en-US（英文）")
        }
)
// 安全配置：JWT Token。也可以配置其他类型的鉴权，比如：basic
@SecurityScheme(
        name = HttpHeaders.AUTHORIZATION,
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)*/
@EnableKnife4j
@Configuration
@EnableOpenApi
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
                // 这个包路径是通过 String.startsWith 方法匹配的，因此需要匹配前缀
                .apis(RequestHandlerSelectors.basePackage("com.example.demo1.controller"))
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
        responseList.add(new ResponseBuilder().code("200").description("操作成功").build());
        /*responseList.add(new ResponseBuilder().code("201").description("修改数据成功").build());
        responseList.add(new ResponseBuilder().code("202").description("命令下发成功").build());
        responseList.add(new ResponseBuilder().code("204").description("操作成功（无返回值）").build());
        responseList.add(new ResponseBuilder().code("400").description("参数错误").build());
        responseList.add(new ResponseBuilder().code("401").description("认证无效，请重新认证").build());
        responseList.add(new ResponseBuilder().code("403").description("权限不够，操作拒绝").build());
        responseList.add(new ResponseBuilder().code("404").description("找不到资源").build());
        responseList.add(new ResponseBuilder().code("405").description("执行方法无效").build());
        responseList.add(new ResponseBuilder().code("429").description("触发限流").build());*/
        return responseList;
    }

}
