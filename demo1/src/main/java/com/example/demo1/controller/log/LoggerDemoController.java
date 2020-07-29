package com.example.demo1.controller.log;

import lombok.extern.shoulder.SLog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 日志使用示例，与 @Slf4j 类似在希望打印的类上添加 {@link SLog} 注解，编译时将生成类似下面代码
 * <code>
 *     private static final org.shoulder.core.log.Logger = org.shoulder.core.log.LoggerFactory.getLogger(LoggerDemoController.class);
 *</code>
 *  没有自动提示？
 *      IDEA：可以安装 shoulder 提供的 lombok-intellij-plugin 插件 <a href="https://gitee.com/ChinaLym/lombok-intellij-plugin" /a>
 *
 *  其他IDE或不想使用 lombok：
 *      使用上面提到的代码替换即可
 *
 * @author lym
 */
@SLog
@RestController
@RequestMapping("log")
public class LoggerDemoController {

    /**
     * 打印日志
     */
    @GetMapping("case1")
    public String messageSource(){
        log.info("this is a example log.");
        return "log is in your console.";
    }

    // todo 补充记录异常、错误码

}
