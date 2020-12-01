package org.shoulder.log.operation.annotation;

import org.shoulder.log.operation.context.OpLogContextHolder;
import org.shoulder.log.operation.context.OperationContextStrategyEnum;
import org.shoulder.log.operation.context.OperationLogFactory;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.enums.TerminalType;

import java.lang.annotation.*;

/**
 * 创建操作日志注解
 * 将该注解加在业务方法上，可以在被注解的方法执行前创建一个操作日志对象，在该方法、调用方法、以及他们创建的线程中使用用 {@link OpLogContextHolder}修改日志。并在该方法执行结束自动记录日志
 * 该注解中的值后续可以修改
 * 无法使用注解时，可以使用 {@link OperationLogFactory#create}
 * <p>
 * 【操作日志框架原理： Spring AOP（spring boot 1.4之后 Spring AOP 的默认实现为 cglib）】
 * <p>
 * 日志框架局限性（即 spring AOP - cglib 的局限性）：
 * CgLib原理为创建目标类子类，因此以下两种情况会导致无法进入AOP
 * 1. 无法创建代理：cglib代理的创建要求目标类能访问和继承，目标方法能被子类覆盖和访问【目标类非 final、非 private，目标方法非 final、非 private】
 * 2. 绕过代理机制：若目标方法非直接调用，而是目标类内部调用，则会绕过代理导致AOP失败。【this指针问题】
 * <p>
 * # 强制走代理方案一，通过 ThreadLocal 获取代理对象
 * 1. 在配置类添加 @EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy=true) 或 application.yml 的
 * spring.aop.两个值都置为true
 * 2. ((YourService)AopContext.currentProxy()).yourMethod();
 * # 强制走代理方案二，若bean为 singleton 的则可使用 @Autowired 注入或 applicationContext.getBean()自身类型自动获取代理对象，通过代理对象调用对应方法
 *
 * @author lym
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作动作标识 【必填】，
     * 如登录、登出、查询、新增、修改、删除、上传、下载等操作。
     * 操作动作标识的多语言词条在多语言翻译文件中提供。
     */
    String operation();

    /**
     * 操作详情多语言 key (选填)
     */
    String detailKey() default "";

    /**
     * 操作内容        (选填)
     * 该字段仅适用于不支持多语言的情况
     * 【支持多语言时，请在代码中编码填充 detailItems {@link OperationLogDTO#addDetailItem} 】
     * <p>
     * 填写 {@link #detailKey} 将忽略该字段，认为支持多语言
     */
    String detail() default "";


    /**
     * 在该方法以默认方式记录所有参数（默认方式中 param.type = STRING ）
     *
     * @see OperationLogParam#value
     */
    boolean logAllParams() default false;

    /**
     * 被操作对象类型 （选填）
     * 【 推荐1：实体、DTO入参、BO 等，实现 Operable，便不必在注解填充该值。
     * 推荐2：在类注解 {@link OperationLogConfig} 上描述该值，便不必在每个方法上填充 】
     */
    String objectType() default "";

    /**
     * 操作者终端类型（选填） 例： TerminalType.BROWSER
     * 【推荐：在类注解 {@link OperationLogConfig} 上描述该值，便不必在每个方法上填充】
     */
    TerminalType terminalType() default TerminalType.BROWSER;

    /**
     * 加了该注解的方法 A 中调用 加了该注解的方法 B 时，日志上下文创建策略
     * 默认，如果不存在嵌套调用，则新建一个上下文。若执行该方法时已经存在日志上下文，则不记录该方法的日志。
     */
    OperationContextStrategyEnum strategy() default OperationContextStrategyEnum.USE_DEFAULT;

    /**
     * 一些通用的操作，用于填充 Operation
     *
     * @author lym
     */
    interface Operations {
        /**
         * 新增
         */
        String ADD = "add";

        /**
         * 修改
         */
        String UPDATE = "update";

        /**
         * 删除
         */
        String DELETE = "delete";

        /**
         * 查询
         */
        String QUERY = "query";

        /**
         * 上传
         */
        String UPLOAD = "upload";

        /**
         * 下载
         */
        String DOWNLOAD = "download";

        /**
         * 导入
         */
        String IMPORT = "import";

        /**
         * 导出
         */
        String EXPORT = "export";

    }

}
