package org.shoulder.web.validate;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.validate.support.dto.FieldValidationRuleDTO;
import org.shoulder.validate.support.extract.ConstraintExtract;
import org.shoulder.validate.support.model.ValidConstraint;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 统一获取表单校验规则
 * 两种拉取方式<br>
 * A表单的保存url为 [POST] http://ip:port/projectName/role/save <br>
 * 第一种（通过增加前缀<font color="red">/api/v1/validate/rule/</font>）<br>
 * 那么获取A表单的验证规则url： [GET] http://ip:port/projectName<font color="red">/api/v1/validate/rule</font>/role/save?method=post <br>
 * <br>
 * 第二种（通过参数传递uri路径的方式来拉取）：<br>
 * [GET] http://ip:port/projectName<font color="red">/api/v1/validate/rule</font>?method=post&uri=/projectName/role/save <br>
 * <br>
 * 固定了验证uri地址，而要验证的表单地址作为参数进行传输。当然，可以一次性拿多个表单验证地址。有些界面可能同时存在多个表单需要提交。
 *
 * @author lym
 */
@Tag(name = "ValidateRuleEndPoint", description = "接口校验规则-查询(只读)；识别后端代码 JSR 校验规则注解，生成校验规则。")
@RestController
@RequestMapping(value = ValidateRuleEndPoint.VALIDATION_RULE_URL_VALUE_EXPRESSION)
public class ValidateRuleEndPoint {

    private final Logger log = ShoulderLoggers.SHOULDER_WEB;

    public static final String VALIDATION_RULE_URL_VALUE_EXPRESSION = "${shoulder.web.ext.dynamic-validate.path:/api/v1/validate/rule}";

    /**
     * 动态路径
     */
    private final String validationRuleUrl;

    /**
     * 请求 mapping
     */
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    /**
     * 约束抽取
     */
    private final ConstraintExtract constraintExtract;

    public ValidateRuleEndPoint(ConstraintExtract constraintExtract,
                                RequestMappingHandlerMapping requestMappingHandlerMapping,
                                String validationRuleUrl) {
        this.validationRuleUrl = validationRuleUrl;
        this.constraintExtract = constraintExtract;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    /**
     * 支持第一种拉取方式
     * 注意： 具体的方法必须在参数上面标注 @Validated 才有效
     *
     * @param request 请求
     * @return 验证规则
     * @throws Exception 异常
     */
    @GetMapping("/**")
    @ResponseBody
    public BaseResult<ListResult<FieldValidationRuleDTO>> viaPathVariable(@RequestParam(value = "method")
                                                                          @Pattern(regexp = "POST|GET|DELETE|PUT|HEAD|PATCH|OPTIONS|TRACE")
                                                                          String method,
                                                                          HttpServletRequest request) throws Exception {
        String requestUri = request.getRequestURI();
        String uri = StrUtil.subAfter(requestUri, validationRuleUrl, false);
        return BaseResult.success(localFieldValidatorDescribe(
                new HttpServletRequestValidatorWrapper(request, method, uri)
        ));
    }

    /**
     * 支持第二种拉取方式
     *
     * @param uri     表单地址
     * @param request 请求
     * @return 验证规则
     * @throws Exception 异常
     */
    @GetMapping
    @ResponseBody
    public BaseResult<ListResult<FieldValidationRuleDTO>> viaQueryParam(@RequestParam(value = "method", required = false) String method,
                                                                        @RequestParam(value = "uri", required = false) String uri,
                                                                        HttpServletRequest request) throws Exception {
        return BaseResult.success(localFieldValidatorDescribe(
                new HttpServletRequestValidatorWrapper(request, method, uri)
        ));
    }

    private List<FieldValidationRuleDTO> localFieldValidatorDescribe(HttpServletRequest request) throws Exception {
        HandlerExecutionChain chains = requestMappingHandlerMapping.getHandler(request);
        if (chains == null) {
            // 避免被黑客拿该接口来探测接口，故若接口不存在，返回内容与无校验规则一致
            log.info("ValidateRuleEndPoint can't find handler match method={}, uri={}", request.getMethod(), request.getRequestURI());
            return Collections.emptyList();
        }
        HandlerMethod method = (HandlerMethod) chains.getHandler();
        log.debug("method is {}", method);
        return loadValidatorDescribe(method);
    }

    /**
     * 伪装成 目标请求，骗过 spring mvc 获取 handler
     */
    public static class HttpServletRequestValidatorWrapper extends HttpServletRequestWrapper {

        private final String method;

        private final String requestUri;

        public HttpServletRequestValidatorWrapper(HttpServletRequest request, String method, String requestUri) {
            super(request);
            this.method = method;
            this.requestUri = requestUri;
        }

        @Override
        public String getRequestURI() {
            return this.requestUri;
        }

        @Override
        public String getServletPath() {
            return this.requestUri;
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    /**
     * Spring 触发校验条件
     * A, 普通对象形：
     * B、@RequestBody形式：
     * <p>
     * 1，类 / 方法 上有 {@link Validated}
     * 2，参数有 {@link Valid} / {@link Validated}
     *
     * <p>
     * C、普通参数形式：
     * 类上有 有 @Validated
     * 参数有 任意注解
     *
     * <p>
     * 步骤：
     * 1，先判断类上是否存在
     * 2，判断方法上是否存在
     * 3，判断
     *
     * @param handlerMethod 处理方法
     * @return 验证规则
     * @throws Exception 异常
     */
    private List<FieldValidationRuleDTO> loadValidatorDescribe(HandlerMethod handlerMethod) throws Exception {
        Method method = handlerMethod.getMethod();
        Parameter[] methodParams = method.getParameters();
        if (methodParams == null || methodParams.length < 1) {
            return Collections.emptyList();
        }
        MethodParameter[] methodParameters = handlerMethod.getMethodParameters();
        if (methodParameters.length < 1) {
            return Collections.emptyList();
        }

        Validated classValidated = method.getDeclaringClass().getAnnotation(Validated.class);

        List<ValidConstraint> validatorStandard = getValidConstraints(methodParams, classValidated);
        return constraintExtract.extract(validatorStandard);
    }

    @Nonnull
    private List<ValidConstraint> getValidConstraints(Parameter[] methodParams, Validated classValidated) {
        List<ValidConstraint> validatorStandard = new ArrayList<>(methodParams.length);
        for (Parameter methodParam : methodParams) {
            Validated methodParamValidate = methodParam.getAnnotation(Validated.class);
            if (classValidated == null && methodParamValidate == null
                    && methodParam.getAnnotation(Valid.class) == null) {
                // 无校验注解
                continue;
            }

            // 优先获取方法上的 验证组，在取类上的验证组
            Class<?>[] groupsOnMethod = null;
            if (methodParamValidate != null) {
                groupsOnMethod = methodParamValidate.value();
            } else if (classValidated != null) {
                groupsOnMethod = classValidated.value();
            }
            validatorStandard.add(new ValidConstraint(methodParam.getType(), groupsOnMethod, methodParam.getDeclaredAnnotations()));
        }
        return validatorStandard;
    }
}

