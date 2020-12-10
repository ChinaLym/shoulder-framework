package com.example.demo1.controller.log;

import com.example.demo1.bo.ShopBO;
import com.example.demo1.enums.MyColorEnum;
import lombok.extern.shoulder.SLog;
import org.shoulder.core.context.AppContext;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.LoggerFactory;
import org.shoulder.core.util.ContextUtils;
import org.shoulder.core.util.StringUtils;
import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.annotation.OperationLogParam;
import org.shoulder.log.operation.dto.OperationLogDTO;
import org.shoulder.log.operation.enums.OperationResult;
import org.shoulder.log.operation.enums.TerminalType;
import org.shoulder.log.operation.logger.OperationLogger;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Date;

/**
 * 操作日志使用示例
 * <p>
 * 通过 @OperationLog、BusinessLogUtils —— 两个类完成操作日志
 *
 * @author lym
 */
@SkipResponseWrap // 该类所有方法的返回值将不被包装
@RestController
@RequestMapping("oplog")
public class OperationLogDemoController {

    /**
     * 定义 shoulder 的 logger， 使用注解 {@link SLog} 时则可不写这行代码
     */
    private static final Logger log = LoggerFactory.getLogger(OperationLogDemoController.class);


    private static final String TIP = "log is in your console.";


    /**
     * 假设有次业务操作了这么一个 BO
     */
    private ShopBO demoBO() {
        ShopBO bo = new ShopBO();
        bo.setId(StringUtils.uuid32());
        bo.setName("shoulder 杂货铺");
        bo.setColor(MyColorEnum.BLUE);
        bo.setAddr("Beijing");
        ShopBO.Owner owner = new ShopBO.Owner("shoulder", 20);
        bo.setBoss(owner);
        bo.setCreateTime(new Date(System.currentTimeMillis()));
        return bo;
    }

    /**
     * <a href="http://localhost:8080/oplog/0" />
     * 不推荐的做法，常规做法，新建了，然后set填充，最后手动执行记录
     */
    @GetMapping("0")
    public String notRecommended() {

        // ... 业务代码 ...

        // 假设这是本次业务被操作的对象(一个商店信息)
        ShopBO operableBo = demoBO();

        // ... 业务代码 ...

        // 操作日志：创建一条日志
        OperationLogDTO opLog = new OperationLogDTO()
                // 填充本次业务修改的对象信息
                .setObjectId(operableBo.getId())
                .setObjectName(operableBo.getName())
                // 为了让目标操作对象类型可以翻译，这里填充多语言 key
                .setObjectType("op.objType.shop.display")

                // 描述是什么操作，补充详情
                .setOperation("op.operation.shop.update.display")
                .setDetailKey("log.actionMessageId.foobar.displayName")
                // 由于详情可以翻译，填充详情中的占位符
                .addDetailItem(operableBo.getBoss().getName())
                .addDetailItem(operableBo.getColor().name())

                // 下面这些内容重复性较高，框架自动帮你填写！

                // 从request中获取客户端类型
                .setTerminalType(TerminalType.BROWSER)
                // 根据请求取出当前用户、并填充用户信息
                .setUserId(AppContext.getUserIdAsString())
                .setUserName(AppContext.getUserName())
                .setUserOrgId("xxx")
                .setUserOrgName("xxx")
                // 租户信息
                .setTenantCode(AppContext.getTenantCode())
                // 记录本次操作结果
                .setResult(OperationResult.SUCCESS)
                .setOperationTime(Instant.now())
                .setEndTime(Instant.now())
                // 可能从调用链中取
                .setTraceId(AppContext.getTranceId());

        // 记录这条日志，关于记录日志的方式也封装了
        // 如记录到 xxx.operation.log 文件（也封装了格式、并支持扩展） / 发送 http 请求到日志中心 / 向 MQ 中发送消息 / 保存到数据库
        ContextUtils
                .getBean(OperationLogger.class)
                .log(opLog);

        return "ok";
    }


    /**
     * <a href="http://localhost:8080/oplog/1" />
     * 通过 @OperationLog 注解，快速生成一条操作日志
     * 只需加个注解、即可记录操作日志
     */
    @GetMapping("1")
    @OperationLog(operation = "testOpLogAnnotation")
    public String case1() {
        return "ok";
    }


    /**
     * <a href="http://localhost:8080/oplog/2" />
     * 添加被操作对象信息时，可以使用接口
     */
    @GetMapping("2")
    @OperationLog(operation = "testOpLogAnnotation")
    public String case2() {
        //OpLogContextHolder.setOperableObject();
        return TIP;
    }


    /**
     * 记录入参 <a href="http://localhost:8080/oplog/4" />
     * 一个注解记录重要参数。默认不支持多语言，参数名为代码中参数名，参数值为 具体参数的toString()
     */
    @OperationLog(operation = "testOpLogAnnotation")
    @GetMapping("4")
    public String case4(
            @RequestParam(defaultValue = "test") @OperationLogParam String param0, // 加了注解
            @RequestParam(defaultValue = "test") @OperationLogParam(supportI18n = true, name = "myParam") String param1, // 设置为支持多语言
            @RequestParam(defaultValue = "test") String param2) { // 未加注解
        return "ok";
    }

    /**
     * 记录入参 <a href="http://localhost:8080/oplog/5" />
     * 在 @OperationLog 注解上，设置 logAllParams = true
     */
    @OperationLog(operation = "testOpLogAnnotation", logAllParams = true)
    @GetMapping("5")
    public String case5(
            @RequestParam(defaultValue = "test") @OperationLogParam String param0, // 加了注解
            @RequestParam(defaultValue = "test") @OperationLogParam(supportI18n = true, name = "myParam") String param1, // 设置为支持多语言
            @RequestParam(defaultValue = "test") String param2) { // 未加注解
        return "ok";
    }


}
