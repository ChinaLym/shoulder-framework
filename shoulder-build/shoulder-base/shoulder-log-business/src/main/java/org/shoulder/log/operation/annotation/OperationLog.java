package org.shoulder.log.operation.annotation;

import org.shoulder.log.operation.util.OperationLogBuilder;
import org.shoulder.log.operation.util.OpLogContextHolder;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 将该注解加在业务方法上，可以在被注解的方法、子方法中、以及从该方法触发的线程中使用用 {@link OpLogContextHolder}修改日志。并在方法执行结束自动记录日志
 * 若无法使用注解时，可以使用 {@link OperationLogBuilder#newLog}
 *
 * 【操作日志框架原理： Spring AOP（spring boot 1.4之后 Spring AOP 的默认实现为 cglib）】
 *
 * 日志框架局限性（即 spring AOP - cglib 的局限性）：
 * CgLib原理为创建目标类子类，因此以下两种情况会导致无法进入AOP
 *      1. 无法创建代理：cglib代理的创建要求目标类能访问和继承，目标方法能被子类覆盖和访问【目标类非 final、非 private，目标方法非 final、非 private】
 *      2. 绕过代理机制：若目标方法非直接调用，而是目标类内部调用，则会绕过代理导致AOP失败。【this指针问题】
 *
 * # 强制走代理方案一，通过 ThreadLocal 获取代理对象
 *      1. 在配置类添加 @EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy=true) 或 application.yml 的
 * spring.aop.两个值都置为true
 *      2. ((YourService)AopContext.currentProxy()).yourMethod();
 * # 强制走代理方案二，若bean为 singleton 的则可使用 @Autowired 注入或 applicationContext.getBean()自身类型自动获取代理对象，通过代理对象调用对应方法
 *
 * @author lym
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作动作标识            例：     UserActions.ADD        【必填】，后续可以覆盖，可暂填写空来绕过检测
     * 表示登录、登出、查询、新增、修改、删除、上传、部署、预览、回放、下载等操作。
     * 操作动作标识的多语言词条在组件封装时提供。
     */
    String action();

    /**
     * 操作详情多语言key   例： ArmingActionMessageIds.BATCH_ARM         (选填)
     * log.i18nKey.<操作内容标识>.message
     */
    String i18nKey() default "";

    /**
     * 不支持多语言，写操作内容         (选填)
     * 【支持多语言时，请在代码中 setActionDetail 或 addActionDetail 来填充，这里仅为不支持多语言的情况提供快速入口。】
     *
     * 填写 actionDetail 且不填写 i18nKey 认为不支持多语言，(自动将多语言置为不支持)
     * 若都填写，将忽略该值
     */
    String actionDetail() default "";


    /**
     * 在该方法以默认方式记录所有参数（默认方式中 param.type = STRING ）
     *
     * @see OperationLogParam#value
     * */
    boolean logAllParam() default false;

    // ==================================== 下方不太常用 =========================================

    /**
     * 对象类型，选填      例：    VmsObjectTypes.CAMERA
     * 【
     * 推荐1：实体实现 Operable，便不必在注解填充。
     * 推荐2：在类注解 @OperationLogConfig 上描述该值，便不必在每个方法上填充
     * 】
     */
    String objectType() default "";

    /**
     * 操作者终端类型，选填 例： OpLogConstants.TerminalType.WEB
     * 【推荐：在类注解 @OperationLogConfig 上描述该值，便不必在每个方法上填充】
     */
    String terminalType() default "";

    /**
     * 是否在抛出异常后自动记录日志，默认记录一条失败日志。
     * 若置为false，则注解所在方法异常后，不自动记录操作日志
     */
    boolean logWhenThrow() default true;

}
