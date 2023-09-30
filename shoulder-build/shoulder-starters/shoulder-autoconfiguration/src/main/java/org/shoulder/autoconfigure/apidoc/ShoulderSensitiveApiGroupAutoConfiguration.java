package org.shoulder.autoconfigure.apidoc;

import org.shoulder.autoconfigure.apidoc.util.RequestHandlerSelectors;
import org.shoulder.core.context.AppInfo;
import org.shoulder.crypto.negotiation.constant.NegotiationConstants;
import org.shoulder.crypto.negotiation.support.Sensitive;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.schema.ModelSpecification;
import springfox.documentation.schema.ScalarModelSpecification;
import springfox.documentation.schema.ScalarType;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 安全 api 接口单独分组
 *
 * @author lym
 */
@AutoConfiguration
@ConditionalOnClass(value = {NegotiationConstants.class, Docket.class})
@ConditionalOnProperty(value = "shoulder.apidoc.sensitive.enable", havingValue = "true", matchIfMissing = true)
public class ShoulderSensitiveApiGroupAutoConfiguration {

    public ShoulderSensitiveApiGroupAutoConfiguration() {
        // just for debug
    }

    /**
     * swagger3的配置文件
     */
    @Bean
    @ConditionalOnMissingBean(value = Docket.class, name = "shoulderSensitiveApiDocket")
    public Docket shoulderSensitiveApiDocket() {
        List<Response> globalResponseMessage = getGlobalResponseMessage();
        return new Docket(DocumentationType.OAS_30)
            .apiInfo(apiInfo())
            .groupName("Sensitive API")
            .select()
            .apis(RequestHandlerSelectors.withAnnotationOnClassOrMethod(Sensitive.class))
            .paths(PathSelectors.any())
            .build()
            .globalRequestParameters(getGlobalRequestParameters())
            .globalResponses(HttpMethod.GET, globalResponseMessage)
            .globalResponses(HttpMethod.POST, globalResponseMessage)
            .globalResponses(HttpMethod.DELETE, globalResponseMessage)
            .globalResponses(HttpMethod.PUT, globalResponseMessage);
    }


    /**
     * 构建 api文档的详细信息函数,注意这里的注解引用的是哪个
     */
    private ApiInfo apiInfo() {
        // 获取工程名称
        String projectName = AppInfo.appId();
        return new ApiInfoBuilder()
            .title(projectName + " 安全API 接口文档")
            .contact(new Contact("lym", "https://github.com/ChinaLym/shoulder-framework", "cn_lym@foxmail.com"))
            .version(AppInfo.version())
            .description("安全API，调用前需要先进行密钥协商，加密传输。")
            .build();
    }

    private List<RequestParameter> getGlobalRequestParameters() {
        List<RequestParameter> parameters = new ArrayList<>();
        parameters.add(new RequestParameterBuilder()
            .name(NegotiationConstants.SECURITY_SESSION_ID)
            .description("安全会话标识")
            .required(true)
            .in(ParameterType.HEADER)
            .build());
        parameters.add(new RequestParameterBuilder()
            .name(NegotiationConstants.TOKEN)
            .description("安全通信-请求签名、防篡改Token")
            .required(true)
            .in(ParameterType.HEADER)
            .build());
        parameters.add(new RequestParameterBuilder()
            .name(NegotiationConstants.SECURITY_DATA_KEY)
            .description("本次请求所使用的数据密钥的密文（通过协商密钥加密）")
            .required(true)
            .in(ParameterType.HEADER)
            .build());
        return parameters;
    }

    /**
     * 生成通用响应信息
     */
    private List<Response> getGlobalResponseMessage() {
        List<Response> responseList = new ArrayList<>();
        responseList.add(new ResponseBuilder().code("200").description("操作成功").build());
        responseList.add(new ResponseBuilder()
            .code("400")
            .description("参数不正确，往往是非法调用")
            .build());
        responseList.add(new ResponseBuilder()
            .code("401")
            .description("认证失败，xSession")
            .headers(Collections.singleton(
                new Header(NegotiationConstants.NEGOTIATION_INVALID_TAG,
                    "标记暗示客户端所使用的会话标识无效，应重新进行密钥协商",
                    new ModelRef("string"),
                    new ModelSpecification(null, null,
                        new ScalarModelSpecification(ScalarType.STRING),
                        null, null, null, null))
                )
            )
            .build());
        return responseList;
    }


}
