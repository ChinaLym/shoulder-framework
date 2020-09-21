package org.shoulder.core.util;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * 打印工具类
 *
 * @author lym
 */
public class PrintUtils {

    /**
     * stackTraceElement 对应的代码位置
     *
     * @param stackTraceElement 方法调用栈
     * @return 可跳转的代码位置
     */
    public static String findCodeLocation(@NonNull StackTraceElement stackTraceElement) {
        // DemoController.method(DemoControllerFileName.java:66)
        int lineNum = stackTraceElement.getLineNumber();
        String classFullName = stackTraceElement.getClassName();
        String classSimpleName = classFullName.substring(classFullName.lastIndexOf(".") + 1);
        String methodName = stackTraceElement.getMethodName();
        String fileName = stackTraceElement.getFileName();
        return classSimpleName + "." + methodName + "(" + fileName + ":" + lineNum + ")";
    }

    public static String findCodeLocation(@NonNull Class<?> clazz, @Nullable String aimMethodName) {
        StackTraceElement stack = findStackTraceElement(clazz, "", false);
        // 肯定会有一个，否则不应该触发该方法 null
        if (stack == null) {
            String tip = StringUtils.isNotBlank(aimMethodName) ?
                "Current StackTrack not contains '" + clazz.getName() + "." + aimMethodName + "()' call!" :
                "Current StackTrack not contains any " + clazz.getName() + "'s method call!";
            throw new IllegalCallerException(tip);
        }
        return findCodeLocation(stack);
    }


    /**
     * 获取当前方法堆栈中调用目标类、方法的堆栈
     *
     * @param aimClass  目标类
     * @param aimMethodName 目标类方法，若 null 则忽略方法校验
     * @param ignoreSubClass 忽略子类
     * @return 未找到返回 null
     */
    @Nullable
    public static StackTraceElement findStackTraceElement(@NonNull Class<?> aimClass,
                                                          @Nullable String aimMethodName, boolean ignoreSubClass) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        boolean foundAimClassFlag = false;
        boolean skipCheckMethod = StringUtils.isEmpty(aimMethodName);
        // 找到一个不是 aimClassName.aimMethodName() 的调用栈
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            // 顺序遍历，层次越来越深
            String stackClassName = stackTraceElement.getClassName();
            boolean isAimClass = aimClass.getName().equals(stackClassName);
            if(isAimClass && ignoreSubClass){
                String classLoadName = stackTraceElement.getClassLoaderName();
                ClassLoader currentClassLoad = Thread.currentThread().getContextClassLoader();
                //if(!classLoadName.equals(currentClassLoad.getName())){
                    // ignore
                //}
                // 根据名称粗略判断，可能存在问题，如子类继承
                try {
                    Class<?> clazz = Class.forName(stackClassName, false, currentClassLoad);
                    isAimClass = aimClass.isAssignableFrom(clazz);
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (!foundAimClassFlag) {
                if (isAimClass) {
                    foundAimClassFlag = skipCheckMethod || aimMethodName.equals(stackTraceElement.getMethodName());
                }
                continue;
            }
            // 加个不能是 c/c++ 的代码（这个条件一般情况下永远为 true）
            boolean filterNot = !isAimClass && (skipCheckMethod || !aimMethodName.equals(stackTraceElement.getMethodName()));
            if (filterNot && !stackTraceElement.isNativeMethod()) {
                return stackTraceElement;
            }
        }
        return null;
    }
}
