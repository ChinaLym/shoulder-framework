package com.example.demo1.controller.log;

import lombok.extern.shoulder.SLog;
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
     * 普通打印日志，直接使用 Slf4j 定义的方法即可，不需要额外学习。
     * 无需定义 logback.xml，开箱即用，shoulder 提供了默认日志格式（见 shoulder-autoconfiguration 的 logback-spring.xml）
     * 使用彩色展示，优化了 logback 的性能
     */
    @GetMapping("0")
    public String notRecommended() {
        log.info("this is a example log.");
        return TIP;
    }

    /**
     * 打印带错误码的日志，目前只有 warn 和 error 级别提供了错误码（更推荐 {@link #case2} 的方式）
     */
    @GetMapping("1")
    public String case1() {
        String errorCode = "0xxxxx1";
        log.warnWithErrorCode(errorCode, "This is a warn log with errorCode");
        log.errorWithErrorCode(errorCode, "This is a error log with errorCode");
        return TIP;
    }

    /**
     * 记录错误码类
     */
    @GetMapping("2")
    public String case2() {

        return TIP;
    }

    // todo 补充记录异常、错误码

}
