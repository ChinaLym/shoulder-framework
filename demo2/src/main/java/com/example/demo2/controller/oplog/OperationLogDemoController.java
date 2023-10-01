package com.example.demo2.controller.oplog;

import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志使用示例----写入DB版本，可以看到代码完全一样，只需要设置 log.operation.logger.type=jdbc 便自动将底层Logger改为写数据库【无感方便】
 * <p>
 * 通过 @OperationLog、OpLogContextHolder —— 两个类完成操作日志
 *
 * @author lym
 */
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@RestController
@RequestMapping("oplog")
public class OperationLogDemoController {

    /**
     * <a href="http://localhost:8080/oplog/1" />
     * 通过 @OperationLog 注解，快速生成一条操作日志
     * 只需加个注解、即可记录操作日志
     */
    @GetMapping("1")
    @OperationLog(operation = "testOpLogAnnotation")
    public String case1() {
        return "log has insertInTo your database.";
    }


}
