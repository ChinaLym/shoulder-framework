package com.example.demo6.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 接口加密
 *
 * @author lym
 */
@Slf4j
@RestController
@RequestMapping("simple")
public class TransportCryptoDemoController {

    /**
     * 测试发起  <a href="http://localhost:80/simple/send"/>
     */
    @GetMapping("send")
    public String send() {
        return null;
    }


    /**
     * 测试直接请求加密接口  <a href="http://localhost:80/simple/receive"/>
     */
    @RequestMapping(value = "receive", method = {RequestMethod.GET, RequestMethod.POST})
    public String receive() {
        return null;
    }


}
