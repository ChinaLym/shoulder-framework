#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import org.shoulder.core.log.Logger;
import org.shoulder.core.log.AppLoggers;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 *
 * @author ${author}
 */
//@SLog // 与 @Slf4j 类似，在希望打日志的类上添加 @SLog 注解，编译时将生成类似下面定义 logger 的代码
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@RestController
@RequestMapping("demo")
public class DemoController {

    /**
     * 定义 shoulder 的 logger， 使用注解 {@link SLog} 时则可不写这行代码
     */
    private static final Logger log = AppLoggers.APP_DEFAULT;


    /**
     * 访问 http://localhost:8080/demo/test 测试
     */
    @GetMapping("test")
    public String test() {
        return "this is a demo controller";
    }


}
