package org.shoulder.core.util;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

/**
 * 打印工具类
 *
 * @author lym
 */
public class PrintUtils {

    /**
     * 获取当前方法堆栈中调用目标类、方法的代码位置
     *
     * @param stackTraceElement 方法调用栈
     * @return 可跳转的代码位置
     */
    public static String fetchCodeLocation(@NonNull StackTraceElement stackTraceElement) {
        // DemoController.method(DemoControllerFileName.java:66)
        int lineNum = stackTraceElement.getLineNumber();
        String classFullName = stackTraceElement.getClassName();
        String classSimpleName = classFullName.substring(classFullName.lastIndexOf(".") + 1);
        String methodName = stackTraceElement.getMethodName();
        String fileName = stackTraceElement.getFileName();
        return classSimpleName + "." + methodName + "(" + fileName + ":" + lineNum + ")";
    }

    public static String fetchCodeLocation(@NonNull String aimClassName, @Nullable String aimMethodName) {
        StackTraceElement stack = findStackTraceElement(RestTemplate.class.getName(), "");
        // 肯定会有一个，否则不应该触发该方法 null
        if(stack == null){
            String tip = StringUtils.isNotBlank(aimMethodName) ?
                "Current StackTrack not contains '" + aimClassName + "." + aimMethodName + "()' call!" :
                "Current StackTrack not contains any " + aimClassName + "'s method call!";
            throw new IllegalCallerException(tip);
        }
        return fetchCodeLocation(stack);
    }


    /**
     * 获取当前方法堆栈中调用目标类、方法的堆栈
     *
     * @param aimClassName  目标类
     * @param aimMethodName 目标类方法，若 null 则忽略方法校验
     * @return 未找到返回 null
     */
    @Nullable
    public static StackTraceElement findStackTraceElement(@NonNull String aimClassName, @Nullable String aimMethodName) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        boolean foundAimClassFlag = false;
        boolean skipCheckMethod = StringUtils.isEmpty(aimMethodName);
        // 找到一个不是 aimClassName.aimMethodName() 的调用栈
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            // 顺序遍历，层次越来越深
            boolean isAimClass = aimClassName.equals(stackTraceElement.getClassName());
            boolean isAimMethod = aimClassName.equals(stackTraceElement.getMethodName());
            if(!foundAimClassFlag){
                if(isAimClass){
                    foundAimClassFlag = skipCheckMethod || isAimMethod;
                }
                continue;
            }
            // 加个不能是 c/c++ 的代码（这个条件一般情况下永远为 true）
            boolean filterNot = !isAimClass && (skipCheckMethod || !isAimMethod);
            if(filterNot && !stackTraceElement.isNativeMethod()){
                return stackTraceElement;
            }
        }
        return null;
    }
}
