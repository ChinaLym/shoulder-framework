package com.example.demo1.controller.concurrent;

import org.shoulder.core.concurrent.Threads;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 线程增强器测试
 *
 * @author lym
 */
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@RestController
@RequestMapping("threadEnhancer")
public class ThreadEnhancerDemoController {

    /**
     * 不建议通过睡眠的方式达到延迟触发的目的，该方式创建出来，在触发前会一直占用一个线程
     * <a href="http://localhost:8080/threadEnhancer/0" />
     */
    @GetMapping("0")
    public String notRecommended() {
        Runnable runnable = () -> System.out.println("这是业务操作 ~~~~~~~~~");
        Threads.execute(runnable);
        return "ok";
    }

    /**
     * 建议使用 Shoulder 中的方式，触发前不会占用线程 <a href="http://localhost:8080/threadEnhancer/1" />
     */
    @GetMapping("1")
    public String case1() {
        SomeBusinessOperation runnable = () -> System.out.println("这是业务操作 ~~~~~~~~~");
        Threads.execute(runnable);
        return "ok";
    }

    public static interface SomeBusinessOperation extends Runnable {

    }

}
