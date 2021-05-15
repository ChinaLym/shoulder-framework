package org.shoulder.web.validate;

import cn.hutool.core.util.StrUtil;
import org.shoulder.core.dto.response.BaseResult;
import org.shoulder.core.dto.response.ListResult;
import org.shoulder.validate.support.dto.FieldValidationRuleDTO;
import org.shoulder.validate.support.extract.ConstraintExtract;
import org.shoulder.validate.support.model.ValidConstraint;
import org.springframework.core.MethodParameter;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 统一获取表单校验规则
 * 两种拉取方式<br>
 * A表单的保存url为 [POST] http://ip:port/projectName/role/save <br>
 * 第一种（通过增加前缀<font color="red">/from/validateRule/</font>）<br>
 * 那么获取A表单的验证规则url： [POST] http://ip:port/projectName<font color="red">/from/validateRule</font>/role/save <br>
 * <br>
 * 第二种（通过参数传递uri路径的方式来拉取）：<br>
 * [GET] http://ip:port/projectName<font color="red">/from/validateRule</font>?formPath=/projectName/role/save <br>
 * <br>
 * 固定了验证uri地址，而要验证的表单地址作为参数进行传输。当然，可以一次性拿多个表单验证地址。有些界面可能同时存在多个表单需要提交。
 *
 * @author lym
 */
@RestController
public class ValidateRuleEndPoint {

    private static final String FORM_VALIDATOR_URL = "/api/v1/validate/rule";

    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    private final ConstraintExtract constraintExtract;

    public ValidateRuleEndPoint(ConstraintExtract constraintExtract, RequestMappingHandlerMapping requestMappingHandlerMapping) {
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
    @RequestMapping(FORM_VALIDATOR_URL + "/**")
    @ResponseBody
    public BaseResult<ListResult<FieldValidationRuleDTO>> standardByPathVar(HttpServletRequest request) throws Exception {
        String requestUri = request.getRequestURI();
        String formPath = StrUtil.subAfter(requestUri, FORM_VALIDATOR_URL, false);
        return BaseResult.success(localFieldValidatorDescribe(request, formPath));
    }

    /**
     * 支持第二种拉取方式
     *
     * @param formPath 表单地址
     * @param request  请求
     * @return 验证规则
     * @throws Exception 异常
     */
    @GetMapping(FORM_VALIDATOR_URL)
    @ResponseBody
    public BaseResult<ListResult<FieldValidationRuleDTO>> standardByQueryParam(@RequestParam(value = "formPath", required = false) String formPath, HttpServletRequest request) throws Exception {
        return BaseResult.success(localFieldValidatorDescribe(request, formPath));
    }

    private List<FieldValidationRuleDTO> localFieldValidatorDescribe(HttpServletRequest request, String formPath) throws Exception {
        HandlerExecutionChain chains = requestMappingHandlerMapping.getHandler(new HttpServletRequestValidatorWrapper(request, formPath));
        if (chains == null) {
            return Collections.emptyList();
        }
        HandlerMethod method = (HandlerMethod) chains.getHandler();
        return loadValidatorDescribe(method);
    }

    /**
     * 伪装成 目标请求，骗过 spring mvc 获取 handler
     */
    public static class HttpServletRequestValidatorWrapper extends HttpServletRequestWrapper {

        private final String formPath;

        public HttpServletRequestValidatorWrapper(HttpServletRequest request, String formPath) {
            super(request);
            this.formPath = formPath;
        }

        @Override
        public String getRequestURI() {
            return this.formPath;
        }

        @Override
        public String getServletPath() {
            return this.formPath;
        }
    }

    /**
     * 官方验证规则： （可能还不完整）
     * A, 普通对象形：
     * B、@RequestBody形式：
     * <p>
     * 1，类或方法或参数上有 @Validated
     * 2，参数有 @Valid
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

        // 类上面的验证注解  handlerMethod.getBeanType().getAnnotation(Validated.class)
        Validated classValidated = method.getDeclaringClass().getAnnotation(Validated.class);

        List<ValidConstraint> validatorStandard = getValidConstraints(methodParams, methodParameters, classValidated);
        return constraintExtract.extract(validatorStandard);
    }

    @Nonnull
    private List<ValidConstraint> getValidConstraints(Parameter[] methodParams, MethodParameter[] methodParameters, Validated classValidated) {
        List<ValidConstraint> validatorStandard = new ArrayList<>(methodParameters.length);
        for (int i = 0; i < methodParameters.length; i++) {
            // 方法上的参数 (能正确获取到 当前类和父类Controller上的 参数类型)
            MethodParameter methodParameter = methodParameters[i];
            // 方法上的参数 (能正确获取到 当前类和父类Controller上的 参数注解)
            Parameter methodParam = methodParams[i];

            Validated methodParamValidate = methodParam.getAnnotation(Validated.class);

            //在参数和类上面找注解
            if (methodParamValidate == null && classValidated == null) {
                continue;
            }

            // 优先获取方法上的 验证组，在取类上的验证组
            Class<?>[] groupsOnMethod = null;
            if (methodParamValidate != null) {
                groupsOnMethod = methodParamValidate.value();
            }
            if (groupsOnMethod == null) {
                groupsOnMethod = classValidated.value();
            }

            validatorStandard.add(new ValidConstraint(methodParameter.getParameterType(), groupsOnMethod));
        }
        return validatorStandard;
    }
}

