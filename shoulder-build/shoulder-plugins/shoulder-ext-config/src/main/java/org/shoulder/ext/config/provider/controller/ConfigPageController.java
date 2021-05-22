package org.shoulder.ext.config.provider.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author lym
 */
@Controller
public class ConfigPageController {

    /**
     * 页面地址
     *
     * @return 配置管理页面
     */
    @GetMapping("/backstage/index")
    public String home() {
        return "configDataManager.vm";
    }


}