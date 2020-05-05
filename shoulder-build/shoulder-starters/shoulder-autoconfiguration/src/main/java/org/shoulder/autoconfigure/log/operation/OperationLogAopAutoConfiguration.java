package org.shoulder.autoconfigure.log.operation;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogConfig;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.covertor.DefaultOperationLogParamValueConverter;
import org.shoulder.log.operation.covertor.OperationLogParamValueConverter;
import org.shoulder.log.operation.covertor.OperationLogParamValueConverterHolder;
import org.shoulder.log.operation.entity.ActionParam;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.log.operation.util.OperationLogBuilder;
import org.shoulder.log.operation.util.OperationLogHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
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
@Configuration
@ConditionalOnClass(OperationLog.class)
@AutoConfigureAfter(value = {
        OperationLogBuilderAutoConfiguration.class,
        OperationLogParamConverterAutoConfiguration.class
})
@EnableConfigurationProperties(OperationLogProperties.class)
public class OperationLogAopAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAopAutoConfiguration.class);

    /**
     * 操作日志记录器
     */
    @Autowired
    OperationLogger OperationLogger;

    @Autowired
    private OperationLogProperties OperationLogProperties;

    @Autowired
    OperationLogParamValueConverterHolder converterHolder;

    /**
     * 用于SpEL表达式解析.
     */
    private SpelExpressionParser parser = new SpelExpressionParser();
    /**
     * 用于获取方法参数定义名字.
     */
    private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    // ********************************* annotation AOP *********************************************

    /**
     * 记录日志开关 隶属于一个注解。注解所在方法执行完毕后就清理
     */
    private ThreadLocal<Boolean> logAfterThrowThreadLocal = new DefaultTrueBooleanThreadLocal(true);


    /**
     * 标识加了 OperationLog 注解的方法
     */
    @Pointcut("@annotation(org.shoulder.log.operation.annotation.OperationLog)")
    public void methodAnnotatedByOperationLog() {
    }

    @Around("methodAnnotatedByOperationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        OperationLogEntity entity = null;
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        // 方法执行前：从注解中解析日志实体
        OperationLog methodAnnotation = method.getAnnotation(OperationLog.class);
        OperationLogConfig classAnnotation = method.getDeclaringClass().getAnnotation(OperationLogConfig.class);
        if (methodAnnotation == null) {
            throw new IllegalStateException("@OperationLog can't be null.");
        }
        // 解析日志
        entity = createLog(methodAnnotation, classAnnotation);
        // 解析日志参数
        entity.setActionParams(createActionParams(entity, joinPoint));

        if (log.isDebugEnabled()) {
            log.debug("auto create a OperationLog: " + entity);
        }
        OperationLogHolder.setLog(entity);

        // ---------------- 执行注解所在目标方法  ------------
        Object o = joinPoint.proceed();
        // ---------------- 注解所在方法正常反回后 -----------

        log.debug("OperationLogUtils.autoLog = " + OperationLogHolder.isEnableAutoLog());
        // 方法执行后： 记录日志 并清除 threadLocal
        if (OperationLogHolder.isEnableAutoLog()) {
            OperationLogger.log();
        }
        cleanLocal();
        return o;
    }

    /**
     * 注解所在方法抛异常后
     * 1. 记录日志
     * 2. 清除 threadLocal
     */
    @AfterThrowing(pointcut = "methodAnnotatedByOperationLog()", throwing = "e")
    public void doAfterThrowing(Throwable e) {
        if (logAfterThrowThreadLocal.get() && OperationLogHolder.isEnableAutoLog()) {
            OperationLogHolder.setResultFail();
            OperationLogger.log();
        }
        cleanLocal();
    }

    // ************************************* 私有方法 ***********************************

    /**
     * 解析注解，从注解创建日志实体
     */
    @NonNull
    private OperationLogEntity createLog(OperationLog methodAnnotation, OperationLogConfig classAnnotation) {

        // 创建日志实体
        OperationLogEntity entity =
                OperationLogBuilder.newLog(methodAnnotation.action());

        // objectType
        if (StringUtils.isNotEmpty(methodAnnotation.objectType())) {
            entity.setObjectType(methodAnnotation.objectType());
        } else if (classAnnotation != null && StringUtils.isNotEmpty(classAnnotation.objectType())) {
            entity.setObjectType(classAnnotation.objectType());
        }

        // terminalType
        if (StringUtils.isNotEmpty(methodAnnotation.terminalType())) {
            entity.setTerminalType(methodAnnotation.terminalType());
        } else if (classAnnotation != null && StringUtils.isNotEmpty(classAnnotation.terminalType())) {
            entity.setTerminalType(classAnnotation.terminalType());
        }

        //多语言、i18nKey、actionDetail
        String i18nKey = methodAnnotation.i18nKey();
        String actionDetail = methodAnnotation.actionDetail();

        if (StringUtils.isNotBlank(i18nKey)) {
            // 填写 i18nKey 代表支持多语言
            entity.setDetailI18nKey(i18nKey);
        }
        if (StringUtils.isNotEmpty(actionDetail)) {
            // 填写 actionDetail 不填写 i18nKey 认为不支持多语言
            entity.setDetail(actionDetail);
        }

        // 是否在抛出异常后自动记录日志
        logAfterThrowThreadLocal.set(methodAnnotation.logAfterThrow());

        return entity;
    }


    /**
     * 解析 actionParams
     *
     * @param entity 解析过的日志实体
     * @param joinPoint 连接点
     * @return 本方法中要记录的参数
     */
    private List<ActionParam> createActionParams(@NonNull OperationLogEntity entity, ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        List<ActionParam> actionParams = new LinkedList<>();
        Parameter[] parameters = method.getParameters();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        OperationLog methodAnnotation = method.getAnnotation(OperationLog.class);
        for (int i = 0; i < parameters.length; i++) {
            OperationLogParam paramAnnotation = parameters[i].getAnnotation(OperationLogParam.class);
            // if args[n] == null or without OperationLogParam Annotation than continue.
            // todo 解析 OperationLog
            if (paramAnnotation == null && !methodAnnotation.logAllParam()) {
                continue;
            }
            ActionParam actionParam = new ActionParam();

            String name = parameterNames[i];
            boolean supportI18n = false;
            String valueSPEL = "";
            Class<? extends OperationLogParamValueConverter> converterClazz =
                    DefaultOperationLogParamValueConverter.class;

            if (paramAnnotation != null) {
                if (StringUtils.isNotBlank(paramAnnotation.name())) {
                    name = paramAnnotation.name();
                }
                supportI18n = paramAnnotation.supportI18n();
                valueSPEL = paramAnnotation.value();
                converterClazz = paramAnnotation.converter();
            }

            actionParam.setName(name);
            actionParam.setI18nValue(supportI18n);
            // setValue
            try {
                if (StringUtils.isNotBlank(valueSPEL)) {
                    // 使用 spel -> value
                    Expression expression = parser.parseExpression(valueSPEL);
                    EvaluationContext context = new StandardEvaluationContext();
                    if(args[i] == null){
                        actionParam.setValue(Collections.singletonList(OperationLogProperties.getNullParamOutput()));
                    } else {
                        context.setVariable(parameterNames[i], args[i]);
                        //for (int i = 0; i < args.length; i++) { }
                        actionParam.setValue(Collections.singletonList(Objects.requireNonNull(
                                expression.getValue(context)).toString()));
                    }

                } else {
                    // 使用 converter
                    OperationLogParamValueConverter converter = converterHolder.getConvert(converterClazz);

                    actionParam.setValue(converter.convert(entity, args[i], parameters[i].getType()));

                }
            } catch (Exception e) {
                log.info("try convert FAIL, but ignored by replace with default value(null). class:'" +
                        method.getDeclaringClass().getName() +
                        "', method:'" + method.getName() +
                        "', paramName=" + parameterNames[i], e);
                // 忽略该参数
                continue;
            }
            actionParams.add(actionParam);
        }
        return actionParams;
    }


    /**
     * 清理日志框架本线程中使用的线程变量
     */
    private void cleanLocal() {
        this.logAfterThrowThreadLocal.remove();
        OperationLogHolder.clean();
    }


    /**
     * 带默认值的 threadLocal
     */
    class DefaultTrueBooleanThreadLocal extends ThreadLocal<Boolean> {
        private final Boolean defaultValue;

        DefaultTrueBooleanThreadLocal(Boolean defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        protected Boolean initialValue() {
            return defaultValue;
        }
    }

}
