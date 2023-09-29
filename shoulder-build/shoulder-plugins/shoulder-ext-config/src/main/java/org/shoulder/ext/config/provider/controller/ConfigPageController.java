package org.shoulder.ext.config.provider.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @deprecated 项目自定义的
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
