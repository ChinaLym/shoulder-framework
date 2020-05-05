package org.shoulder.log.operation.util;


import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.shoulder.log.operation.constants.OpLogConstants;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.lang.Nullable;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * 操作日志实体构建器
 *
 * （当前主要面向框架内部，不建议使用者调用该类中的方法，方法名称和参数不稳定）推荐使用注解方式。
 *
 * 在使用前必须先调用 {@link #init}！
 *
 * @author lym
 */
public class OperationLogBuilder {

    /**
     * 内部任务 userId
     * 默认：service.应用名称 如：service.uaa
     */
    private static String innerTaskUserId;

    /** 默认值： @Value("${spring.application.name:}") */
    private static String serviceId = null;

    /** 保存当前用户信息 */
    private static ThreadLocal<Operator> currentOperatorThreadLocal = new ThreadLocal<>();

    /** 初始化标记 */
    private static boolean notInitialized = false;

    /**
     * 在使用前必须先调用本方法！
     * 设置本组件的 组件标识、段标识、段实例标识
     */
    public static void init(String serviceId) {
        Logger logger = LoggerFactory.getLogger(OperationLogBuilder.class);
        if(StringUtils.isEmpty(serviceId)){
            logger.warn("serviceId is empty!");
        }

        innerTaskUserId = "service." + serviceId;
        notInitialized = false;

        logger.info("OperationLogBuilder initialized! serviceId=" + serviceId);
    }


    /**
     * 创建一条操作日志
     * */
    public static OperationLogEntity newLog(){
        return newLog(null);
    }

    /**
     * 创建一条操作日志
     *  1. 创建对象
     *  2. 填充为内部任务日志
     *  3. 若上下文存在登录的用户则将其当做操作者
     */
    public static OperationLogEntity newLog(String action){
        return decorateWithInnerTask(new OperationLogEntity(action)).setOperator(currentOperatorThreadLocal.get());
    }

    /**
     * 1. 根据 OperationLogTemplate 创建 operableList.size() 条操作日志
     * 2. 用 operableList 分别填充每条日志的被操作对象和 actionDetail 字段
     *
     *      如果 operableList 为空，则返回日志模板 OperationLogTemplate。
     *
     * @param OperationLogTemplate 日志模板
     * @param operableList        被操作对象集合
     * @return 多条填充完毕的日志对象，至少返回包含一条日志的 List
     * */
    public static List<OperationLogEntity> newLogsFrom(OperationLogEntity OperationLogTemplate,
                                                       @Nullable Collection<? extends Operable> operableList){
        if(OperationLogTemplate == null || CollectionUtils.isEmpty(operableList)){
            return Collections.singletonList(OperationLogTemplate);
        }
        List<OperationLogEntity> resultLogs = new ArrayList<>(operableList.size());
        for (Operable operable : operableList) {
            OperationLogEntity logEntity = OperationLogTemplate.clone();
            resultLogs.add(logEntity.setOperableObject(operable));
        }
        return resultLogs;
    }


    // ===========================================================

    /** 默认的构造器 */
    private static OperationLogEntity decorateWithInnerTask(OperationLogEntity opLogEntity){
        opLogEntity.setIp(LOCAL_IP);
        opLogEntity.setMac(LOCAL_MAC);
        opLogEntity.setUserId(getInnerTaskUserId());
        opLogEntity.setUserName(null)
                .setPersonId(null)
                .setTerminalType(OpLogConstants.TerminalType.SYSTEM)
                .setUserOrgId(null)
                .setServiceId(serviceId)
                .setTraceId(getCurrentTraceId());

        return opLogEntity;
    }

    /** todo 获取traceId */
    private static String getCurrentTraceId(){
        // 从 MDC 中获取 traceId 和 spanId
        return MDC.get("traceId");
    }

    // ------------------------------------------------------------

    /**
     * 获取内部任务的 userId 前必须先 set。
     */
    public static String getInnerTaskUserId() {
        if(notInitialized){
            throw new IllegalStateException("Please call init() first!");
        }
        return innerTaskUserId;
    }

    public static void setDefaultOperator(Operator operator) {
        currentOperatorThreadLocal.set(operator);
    }

    public static Operator getDefaultOperator() {
        return currentOperatorThreadLocal.get();
    }


    /**
     * 清理线程变量
     * 推荐由拦截器负责
     * @deprecated 使用者不要在业务代码中调用该方法，除非你很清楚日志框架的原理
     */
    public static void clean() {
        currentOperatorThreadLocal.remove();
    }


    private static String LOCAL_IP = getLocalIP();
    private static String LOCAL_MAC = getLocalMAC();

    /**
     * 获取本机ip
     * @return ip ：10.10.10.10
     */
    private static String getLocalIP() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }



    /**
     * 获取本机MAC地址，推荐从缓存中获取
     * @return mac：2C-4D-54-E5-86-0E
     */
    private static String getLocalMAC() {
        try {
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<mac.length;i++) {
                if(i!=0){
                    sb.append("-");
                }
                //mac[i] & 0xFF 是为了把byte转化为正整数
                String s = Integer.toHexString(mac[i] & 0xFF);
                sb.append(s.length() == 1 ? 0 + s : s);
            }

            return sb.toString().toUpperCase();
        } catch (Exception e) {
            return "00-00-00-00-00-00";
        }
    }
}
