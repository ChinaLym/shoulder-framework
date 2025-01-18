package ${package}.controller;

import org.shoulder.core.i18.Translator;
import org.shoulder.core.log.AppLoggers;
import org.shoulder.core.log.Logger;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RestController 示例
 * <p><a href="https://doc.itlym.cn/shoulder_web.html">Shoulder Web 指南</a>
 *
 * @author ${author}
 */
@RestController
@RequestMapping("demo")
public class DemoController {

    /**
     * 定义 shoulder 的 logger， 使用注解 {@link SLog} 时则可不写这行代码
     */
    private static final Logger log = AppLoggers.APP_DEFAULT;

    @Autowired
    private Translator translator;

    /**
     * 访问 <a href="http://localhost:8080/demo/hello">http://localhost:8080/demo/hello</a> 进行 controller 测试
     */
    @SkipResponseWrap // 跳过响应值包装
    @GetMapping("hello")
    public String hello() {
        return "this is a demo controller";
    }


    /**
     * 访问 <a href="http://localhost:8080/demo/api">http://localhost:8080/demo/api</a> 进行 api 测试
     */
    @GetMapping("api")
    public String api() {
        return "this is api data";
    }

    /**
     * 访问 <a href="http://localhost:8080/demo/translate">http://localhost:8080/demo/translate</a> 进行翻译测试
     */
    @GetMapping("translate")
    public String translate() {
        return translator.getMessage("shoulder.test.hello", "${artifactId}" + " | autoParseLanguage=" + translator.currentLocale());
    }

}
