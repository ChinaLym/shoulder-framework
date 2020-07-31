package com.example.demo1.controller.delay;

import lombok.extern.shoulder.SLog;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 延迟任务使用示例
 *
 *
 * @author lym
 */
@SLog
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@RestController
@RequestMapping("delay")
public class DelayTaskDemoController {

    /** 定义 shoulder 的 logger， 使用注解 {@link SLog} 时则可不写这行代码 */
    private static final Logger log = LoggerFactory.getLogger(DelayTaskDemoController.class);


    private static final String TIP = "5秒中后，控制台将输出一条日志";

    /**
     * 不建议通过睡眠的方式达到延迟触发的目的
     */
    @GetMapping("0")
    public String notRecommended(){
        Thread delay = new Thread(() -> {
            // 5s 后触发
            Thread.sleep(5000);
            log.warn("I'am a delay Task");
        });

        return TIP;
    }

    /**
     * 打印带错误码的日志，目前只有 warn 和 error 级别提供了错误码（更推荐 {@link #case2} 的方式）
     */
    @GetMapping("1")
    public String case1(){
        String errorCode = "0xxxxx1";
        log.warnWithErrorCode(errorCode, "This is a warn log with errorCode");
        log.errorWithErrorCode(errorCode, "This is a error log with errorCode");
        return TIP;
    }

}
