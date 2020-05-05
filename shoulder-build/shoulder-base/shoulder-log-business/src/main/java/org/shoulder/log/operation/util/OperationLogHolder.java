package org.shoulder.log.operation.util;

import org.shoulder.log.operation.annotation.OperationLog;
import org.shoulder.log.operation.constants.OperateResultEnum;
import org.shoulder.log.operation.dto.Operable;
import org.shoulder.log.operation.dto.Operator;
import org.shoulder.log.operation.entity.OperationLogEntity;
import org.springframework.lang.Nullable;

import java.util.*;

/**
 * 该类充当 OperationLogHolder 的角色。set日志对象后，可以通过该工具在同一线程内操作这个日志对象。
 * 主要为了配合 {@link OperationLog}注解的使用
 * 若组件希望扩展该类能力：允许组件以继承或组合方式调用类，并使用新的类，增加扩展能力。
 *
 * @author lym
 */
public class OperationLogHolder {

    /** 保存日志实体的线程变量 */
    private static ThreadLocal<OperationLogEntity> opLogLocal = new ThreadLocal<>();

    /** 保存被操作对象的线程变量 */
    private static ThreadLocal<List<Operable>> operableObjectsLocal = new ThreadLocal<>();

    /** 是否在方法结束后自动记录日志 */
    private static ThreadLocal<Boolean> autoLog = new ThreadLocal<>();


    // ============================ 获取线程中的操作日志变量 =======================

    /**
     * 直接从 threadLocal 获取日志实体
     * 一般不会在组件或业务方法中使用，仅在极个别情况使用，如框架自己的 AbstractOpLogAsyncRunner
     *
     * 除非你很清楚你为什么不要框架检查调用时机，否则不建议使用该方法！
     * */
    @Nullable
    public static OperationLogEntity getLogWithoutCheck() {
        return opLogLocal.get();
    }


    /**
     * 获取线程中日志，若为 null，则抛异常
     *      通常，抛异常代表使用者错误地使用了 spring AOP。如少了注解，或者未走 spring 代理
     * @return 线程中日志
     */
    public static OperationLogEntity getLog() {
        OperationLogEntity log = opLogLocal.get();
        if (log == null) {
            throw new IllegalThreadStateException(
                    "No OperationLogEntity in concurrentThread! Maybe your method miss @OperationLog " +
                            "or used [SpringAop] incorrectly! Thread - [" + Thread.currentThread().getName() + "]. " +
                            "Helpful Tip: Add a breakpoint here and debug your code.");
        }
        return log;
    }

    /**
     * 设置到 threadLocal
     */
    public static void setLog(OperationLogEntity entity) {
        opLogLocal.set(entity);
    }

    // ============================ 获取线程中的被操作对象【用于批量操作】 =======================

    /**
     * 获取线程中所有被操作对象
     * */
    public static List<? extends Operable> getOperableObjects() {
        return operableObjectsLocal.get();
    }

    /**
     * 批量设置被操作对象
     * */
    public static void setOperableObjects(Collection<? extends Operable> operableObjs) {
        if(operableObjs != null){
            operableObjectsLocal.set(new ArrayList<>(operableObjs));
        }
    }

    /**
     * 添加多个被操作对象
     * */
    public static void addOperableObjects(Collection<? extends Operable> operableObjs) {
        if(operableObjs != null){
            List<Operable> local = operableObjectsLocal.get();
            if(local == null){
                operableObjectsLocal.set(new ArrayList<>(operableObjs));
            }else {
                local.addAll(operableObjs);
            }
        }
    }

    /**
     * 添加被操作对象
     * */
    public static void addOperableObject(Operable operableObj) {
        List<Operable> local = operableObjectsLocal.get();
        if(local == null){
            List<Operable> list = new LinkedList<>();
            list.add(operableObj);
            operableObjectsLocal.set(list);
        }else {
            local.add(operableObj);
        }
    }

    // ============================ 记录日志开关 =========================

    /**
     * 是否开启自动记录日志
     */
    public static boolean isEnableAutoLog() {
        // 默认开启
        return autoLog.get() == null ?
                true :
                autoLog.get();
    }

    /**
     * 临时关闭自动记录日志，一般用于表示希望 @OperationLog 只生成操作日志对象，而不记录。
     *
     * 场景：批量导入时，使用新线程对导入内容的校验，日志记录也在新线程中做， 处理请求的方法中不希望记录操作日志
     *      在方法中调用 closeAutoLog，告诉框架在本方法结束后不要记录操作日志
     */
    public static void closeAutoLog() {
        autoLog.set(false);
    }

    /**
     * 重新开启自动记录日志
     */
    public static void enableAutoLog() {
        autoLog.set(true);
    }

    // ==================== 设置日志实体的属性，按照从常用到不常用的顺序排列 ===========================

    /**
     * 填充单个被操作对象信息
     */
    public static OperationLogEntity setOperableObject(Operable operable){
        return getLog().setOperableObject(operable);
    }

    /**
     * 设置结果为失败
     */
    public static OperationLogEntity setResultFail(){
        return getLog().setResult(OperateResultEnum.FAIL);
    }

    /**
     * 设置结果
     */
    public static OperationLogEntity setResult(OperateResultEnum result) {
        return getLog().setResult(result);
    }

    /**
     * 设置被操作对象ID,
     */
    public static OperationLogEntity setObjectId(String objectId){
        return getLog().setObjectId(objectId);
    }

    /**
     * 设置被操作对象名称
     */
    public static OperationLogEntity setObjectName(String objectName){
        return getLog().setObjectName(objectName);
    }

    /**
     * 设置对象类型
     */
    public static OperationLogEntity setObjectType(String objectType){
        return getLog().setObjectType(objectType);
    }

    /**
     * 设置 action
     */
    public static OperationLogEntity setAction(String action) {
        return getLog().setAction(action);
    }

    /**
     * 设置多语言消息ID
     */
    public static OperationLogEntity setActionMessageId(String i18nKey){
        return getLog().setDetailI18nKey(i18nKey);
    }

    /**
     * 设置操作详情
     */
    public static OperationLogEntity setActionDetail(List<String> actionDetails){
        return getLog().setDetailItem(actionDetails);
    }

    /**
     * 添加操作详情
     */
    public static OperationLogEntity addActionDetail(String actionDetail){
        return getLog().addDetailItem(actionDetail);
    }

    /**
     * 填充操作者信息
     * */
    public static OperationLogEntity setOperator(Operator operator){
        return getLog().setOperator(operator);
    }

    /**
     * 设置关联ID
     */
    public static OperationLogEntity setRelationId(String relationId){
        return getLog().setRelationId(relationId);
    }


    /**
     * 线程变量清理：
     * @deprecated 除非你很明白框架原理，否则不要主动调用该方法
     */
    public static void clean(){
        opLogLocal.remove();
        autoLog.remove();
        operableObjectsLocal.remove();
    }
}
