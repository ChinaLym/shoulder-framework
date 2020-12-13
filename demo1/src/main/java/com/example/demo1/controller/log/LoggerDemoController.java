package com.example.demo1.controller.log;

import lombok.extern.shoulder.SLog;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志使用示例
 * <p>
 * 为了简化日志记录错误码，shoulder 在 Slf4j 的 Logger 接口上额外添加了记录错误码的方法
 * 且可灵活替换为任何一个日志框架（替换参见 {@link LoggerFactory} 的 spi 机制）
 * <p>
 * 习惯了 lombok，不想写这行代码？
 * 把原来的 {@link lombok.extern.slf4j.Slf4j} 替换为 {@link SLog} 即可（Shoulder 为你量身定制了 lombok 源码）
 * <p>
 * {@link SLog} 没有自动提示？
 * IDEA 中可以安装 shoulder 提供的 lombok-intellij-plugin 插件
 * <a href="https://gitee.com/ChinaLym/lombok-intellij-plugin" /a>
 * 该插件在原 lombok 插件基础上新增了支持 Shoulder 的扩展，且 100% 支持原有功能
 *
 * @author lym
 */
@SLog // 与 @Slf4j 类似，在希望打日志的类上添加 @SLog 注解，编译时将生成类似下面定义 logger 的代码
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@RestController
@RequestMapping("log")
public class LoggerDemoController {

    /**
     * 定义 shoulder 的 logger， 使用注解 {@link SLog} 时则可不写这行代码
     */
    private static final Logger log = LoggerFactory.getLogger(LoggerDemoController.class);


    private static final String TIP = "log is in your console.";

    /**
     * <a href="http://localhost:8080/log/0" />
     * 普通打印日志，直接使用 Slf4j 定义的方法即可，不需要额外学习。
     * 无需定义 logback.xml，开箱即用，shoulder 提供了默认日志格式（见 shoulder-autoconfiguration 的 logback-spring.xml）
     * 使用彩色展示，优化了 logback 的性能
     */
    @GetMapping("0")
    public String notRecommended() {
        log.info("this is a example log. 我是一条日志~~~~");
        return TIP;
    }

    /**
     * <a href="http://localhost:8080/log/1" />
     * 打印带错误码的日志，目前只有 warn 和 error 级别提供了错误码（更推荐 {@link #case2} 的方式）
     */
    @GetMapping("1")
    public String case1() {
        String errorCode = "0xxxxx1";
        log.warnWithErrorCode(errorCode, "This is a warn log with errorCode. 我是一条警告日志~~~~");
        log.errorWithErrorCode(errorCode, "This is a error log with errorCode. 我是一条错误日志~~~~");
        return TIP;
    }

    /**
     * <a href="http://localhost:8080/log/2" />
     * 记录错误码类【推荐】
     * 只需要记录 ErrorCode 接口即可
     */
    @GetMapping("2")
    public String case2() {
        log.error(CommonErrorCodeEnum.UNKNOWN);
        return TIP;
    }

    /**
     * <a href="http://localhost:8080/log/3" />
     * 记录错误码类【推荐】
     * 只需要记录 ErrorCode 接口即可
     */
    @GetMapping("3")
    public String case3() {
        try {
            String nullStr = null;
            // 这里会抛 NPE
            int a = nullStr.length();
        } catch (NullPointerException e) {

            // 记录堆栈：适用于捕获意料之外的错误，需要排差
            log.error(CommonErrorCodeEnum.UNKNOWN, e);

            // 不记录堆栈：适用于捕获意料之中的错误，不需要排差，程序中已经有处理措施，如读取配置时，发现无该配置，使用默认值
            log.error(CommonErrorCodeEnum.UNKNOWN);

            // 显示指定错误码
            String errorCode = CommonErrorCodeEnum.UNKNOWN.getCode();
            log.errorWithErrorCode(errorCode, "发生异常了");
            log.info("=============================");
            log.errorWithErrorCode(errorCode, "发生 {} 异常了", e.getClass().getSimpleName());
            log.info("=============================");
            log.errorWithErrorCode(errorCode, "发生 {} 异常了，message={}", e.getClass().getSimpleName(), e.getMessage());
            log.info("=============================");
            log.errorWithErrorCode(errorCode, "{} 发生 {} 异常了，message={}", "case3", e.getClass().getSimpleName(), e.getMessage());
            log.info("=============================");
            log.errorWithErrorCode(errorCode, "发生异常了", "case3", e);
            log.info("==========================================================");
        }

        return TIP;
    }

}
