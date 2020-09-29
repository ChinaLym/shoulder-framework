package org.shoulder.autoconfigure.operation;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogConfig;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.dto.OpLogParam;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.format.covertor.DefaultOperationLogParamValueConverter;
import org.shoulder.log.operation.format.covertor.OperationLogParamValueConverter;
import org.shoulder.log.operation.format.covertor.OperationLogParamValueConverterHolder;
import org.shoulder.log.operation.util.OpLogContext;
import org.shoulder.log.operation.util.OpLogContextHolder;
import org.shoulder.log.operation.util.OperationLogBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 * 激活操作日志 OperationLog 注解 AOP
 *
 * @author lym
 */
@Aspect
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(OperationLogDTO.class)
@AutoConfigureAfter(value = {
    OperationLoggerAutoConfiguration.class,
    OperationLogParamConverterAutoConfiguration.class
})
@EnableConfigurationProperties(OperationLogProperties.class)
public class OperationLogAspect {

    private final static Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    /**
     * 保存操作日志上次的上下文
     */
    private static ThreadLocal<OpLogContext> lastOpLogContext = new ThreadLocal<>();
    @Autowired
    private OperationLogProperties operationLogProperties;

    //用于获取方法参数定义名字.
    //private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
    /**
     * 用于SpEL表达式解析.
     */
    private SpelExpressionParser parser = new SpelExpressionParser();

    // ********************************* annotation AOP *********************************************

    /**
     * 标识加了 OperationLog 注解的方法
     */
    @Pointcut("@annotation(org.shoulder.log.operation.annotation.OperationLog)")
    public void methodAnnotatedByOperationLog() {
    }

    @Around("methodAnnotatedByOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        // 保存之前的日志上下文信息
        stashLastContext();

        // 创建日志上下文
        creatNewContext(joinPoint);

        // ---------------- 执行注解所在目标方法  ------------
        Object o = joinPoint.proceed();
        // ---------------- 注解所在方法正常反回后 -----------

        log.debug("OperationLogUtils.autoLog={}", OpLogContextHolder.isEnableAutoLog());

        // 方法执行后： 记录日志 并清除 threadLocal
        if (OpLogContextHolder.isEnableAutoLog()) {
            OpLogContextHolder.log();
        }

        // 恢复之前的上下文
        popLastContext();
        return o;
    }

    /**
     * 注解所在方法抛异常后
     * 1. 记录日志
     * 2. 清除 threadLocal
     */
    @AfterThrowing(pointcut = "methodAnnotatedByOperationLog()")
    public void doAfterThrowing() {
        if (OpLogContextHolder.isEnableAutoLog() && OpLogContextHolder.isLogWhenThrow()) {
            OpLogContextHolder.getLog().setResultFail();
            OpLogContextHolder.log();
        }

        // 恢复之前的上下文
        popLastContext();
    }

    // ************************************* 私有方法 ***********************************

    /**
     * 进方法前存储的之前的日志上下文
     */
    private void stashLastContext() {
        lastOpLogContext.set(OpLogContextHolder.getContext());
    }

    private void creatNewContext(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        // 解析注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        OperationLog methodAnnotation = method.getAnnotation(OperationLog.class);
        OperationLogConfig classAnnotation = method.getDeclaringClass().getAnnotation(OperationLogConfig.class);
        if (methodAnnotation == null) {
            // spring aop使用cglib生成的代理是不会加上父类的方法上的注解的，也就是这边生成的代理类上的方法上没有 OperationLog 注解
            method = joinPoint.getTarget().getClass().getMethod(method.getName(), method.getParameterTypes());
            methodAnnotation = method.getAnnotation(OperationLog.class);
            if (methodAnnotation == null) {
                // 不可能的情况，因为日志 AOP 就是以该注解为切点，需要检查 aspect 表达式
                throw new IllegalStateException("@OperationLog can't be null.");
            }

        }

        // 创建日志
        OperationLogDTO entity = createLog(joinPoint, methodAnnotation, classAnnotation);

        if (log.isDebugEnabled()) {
            log.debug("auto create a OperationLog: " + entity);
        }

        OpLogContext context = OpLogContext.create(lastOpLogContext.get()).setOperationLog(entity);

        // 是否在抛出异常后自动记录日志

        OpLogContextHolder.setContext(context);
    }


    /**
     * 执行方法后，清理本方法的上下文，恢复上次的上下文
     */
    private void popLastContext() {
        OpLogContext lastContext = lastOpLogContext.get();
        if (lastContext != null) {
            OpLogContextHolder.setContext(lastContext);
        } else {
            OpLogContextHolder.clean();
        }
        lastOpLogContext.remove();
    }

    /**
     * 根据注解创建日志实体
     */
    @NonNull
    private OperationLogDTO createLog(ProceedingJoinPoint joinPoint, OperationLog methodAnnotation, OperationLogConfig classAnnotation) {
        // 创建日志实体
        OperationLogDTO entity =
            OperationLogBuilder.newLog(methodAnnotation.operation());

        // objectType
        if (StringUtils.isNotEmpty(methodAnnotation.objectType())) {
            entity.setObjectType(methodAnnotation.objectType());
        } else if (classAnnotation != null && StringUtils.isNotEmpty(classAnnotation.objectType())) {
            entity.setObjectType(classAnnotation.objectType());
        }

        // terminalType
        entity.setTerminalType(methodAnnotation.terminalType());

        // 操作详情
        String detailI18nKey = methodAnnotation.detailKey();
        String detail = methodAnnotation.detail();

        if (StringUtils.isNotBlank(detailI18nKey)) {
            // 填写则表示支持多语言
            entity.setDetailKey(detailI18nKey);
        }
        if (StringUtils.isNotEmpty(detail)) {
            // 填写 detail 不填写 detailI18nKey 认为不支持多语言
            entity.setDetail(detail);
        }

        // 解析日志参数
        entity.setParams(createOperationParams(entity, joinPoint));

        return entity;
    }


    /**
     * 解析操作参数
     *
     * @param entity    解析过的日志实体
     * @param joinPoint 连接点
     * @return 本方法中要记录的参数
     */
    private List<OpLogParam> createOperationParams(@NonNull OperationLogDTO entity, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        List<OpLogParam> opLogParams = new LinkedList<>();
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        OperationLog methodAnnotation = method.getAnnotation(OperationLog.class);
        for (int i = 0; i < parameters.length; i++) {
            OperationLogParam paramAnnotation = parameters[i].getAnnotation(OperationLogParam.class);
            // if args[n] == null or without OperationLogParam Annotation than continue.
            if (paramAnnotation == null && !methodAnnotation.logAllParam()) {
                continue;
            }
            OpLogParam opLogParam = new OpLogParam();

            String name = parameterNames[i];
            boolean supportI18n = false;
            // Spring Expression Language
            String valueSpEL = "";
            Class<? extends OperationLogParamValueConverter> converterClazz =
                DefaultOperationLogParamValueConverter.class;

            if (paramAnnotation != null) {
                if (StringUtils.isNotBlank(paramAnnotation.name())) {
                    name = paramAnnotation.name();
                }
                supportI18n = paramAnnotation.supportI18n();
                valueSpEL = paramAnnotation.value();
                converterClazz = paramAnnotation.converter();
            }

            opLogParam.setName(name);
            opLogParam.setSupportI18n(supportI18n);
            // setValue
            try {
                if (StringUtils.isNotBlank(valueSpEL)) {
                    // 使用 spel -> value
                    Expression expression = parser.parseExpression(valueSpEL);
                    EvaluationContext context = new StandardEvaluationContext();
                    if (args[i] == null) {
                        opLogParam.setValue(Collections.singletonList(operationLogProperties.getNullParamOutput()));
                    } else {
                        context.setVariable(parameterNames[i], args[i]);
                        //for (int i = 0; i < args.length; i++) { }
                        opLogParam.setValue(Collections.singletonList(Objects.requireNonNull(
                            expression.getValue(context)).toString()));
                    }

                } else {
                    // 使用 converter
                    OperationLogParamValueConverter converter = OperationLogParamValueConverterHolder.getConvert(converterClazz);

                    opLogParam.setValue(converter.convert(entity, args[i], parameters[i].getType()));

                }
            } catch (Exception e) {
                log.info("try convert FAIL, but ignored by replace with default value(null). class:'" +
                    method.getDeclaringClass().getName() +
                    "', method:'" + method.getName() +
                    "', paramName=" + parameterNames[i], e);
                // 忽略该参数
                continue;
            }
            opLogParams.add(opLogParam);
        }
        return opLogParams;
    }

}
