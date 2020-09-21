package org.shoulder.http.util;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.shoulder.core.util.ColorString;
import org.shoulder.core.util.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 请求日志美化
 *
 * @author lym
 */
public class HttpLogHelper {


    /**
     * 组装颜色字符串
     *
     * @param cost 毫秒
     * @return 颜色字符串
     */
    public static ColorString cost(long cost) {
        return new ColorString(cost + "ms").color(costColor(cost));
    }

    /**
     * 请求花费时间的颜色
     *
     * @param cost 毫秒
     * @return 颜色
     */
    public static int costColor(long cost) {
        return cost < 200 ? ColorString.GREEN :
            cost < 1000 ? ColorString.YELLOW :
                ColorString.RED;
    }

    /**
     * 响应状态颜色
     *
     * @param httpStatus 状态
     * @return 颜色
     */
    public static int httpStatusColor(String httpStatus) {
        // 成功
        return httpStatus.startsWith("2") ? ColorString.GREEN :
            // 服务器出错
            httpStatus.startsWith("5") ? ColorString.BLUE :
                // 客户端出错
                httpStatus.startsWith("4") ? ColorString.RED : ColorString.YELLOW;
    }

    public static void appendHeader(Map<String, String> headers) {

    }

    /**
     * IDE 控制台日志跳转代码原理：https://www.jetbrains.com/help/idea/setting-log-options.html
     * 输出这种格式则可以识别 <fully-qualified-class-name>.<method-name>(<file-name>:<line-number>)
     * 但测试发现 IDEA 只要 .(xxx.java:行号) 格式即可
     *
     * @param method 方法
     * @return IDE 支持的跳转格式
     */
    public static String genCodeLocationLink(Method method) {
        Class<?> clazz = method.getDeclaringClass();

        int lineNum;
        try {
            CtClass ctClass = ClassPool.getDefault().get(clazz.getName());
            CtMethod ctMethod = ctClass.getDeclaredMethod(method.getName());
            lineNum = ctMethod.getMethodInfo().getLineNumber(0);
        } catch (NotFoundException e) {
            // 未找到，无法跳到具体行数，使用第一行，以便于跳到目标类
            lineNum = 1;
        }
        return clazz.getSimpleName() + "." + method.getName() +
            "(" + getClassFileName(clazz) + ".java:" + lineNum + ")";

    }

    private static String getClassFileName(Class clazz) {
        String classFileName = clazz.getName();
        final String split = "$";
        if (classFileName.contains(split)) {
            int indexOf = classFileName.contains(".") ? classFileName.lastIndexOf(".") + 1 : 0;
            return classFileName.substring(indexOf, classFileName.indexOf("$"));
        } else {
            return clazz.getSimpleName();
        }
    }

    /**
     * stackTraceElement 对应的代码位置
     *
     * @param stackTraceElement 方法调用栈
     * @return 可跳转的代码位置
     */
    public static String genCodeLocationLinkFromStack(@NonNull StackTraceElement stackTraceElement) {
        // DemoController.method(DemoControllerFileName.java:66)
        int lineNum = stackTraceElement.getLineNumber();
        String classFullName = stackTraceElement.getClassName();
        String classSimpleName = classFullName.substring(classFullName.lastIndexOf(".") + 1);
        String methodName = stackTraceElement.getMethodName();
        String fileName = stackTraceElement.getFileName();
        return classSimpleName + "." + methodName + "(" + fileName + ":" + lineNum + ")";
    }

    public static String genCodeLocationLinkFromStack(@NonNull Class<?> clazz, @Nullable String aimMethodName) {
        StackTraceElement stack = findStackTraceElement(clazz, "", false);
        // 肯定会有一个，否则不应该触发该方法 null
        if (stack == null) {
            String tip = StringUtils.isNotBlank(aimMethodName) ?
                "Current StackTrack not contains '" + clazz.getName() + "." + aimMethodName + "()' call!" :
                "Current StackTrack not contains any " + clazz.getName() + "'s method call!";
            throw new IllegalCallerException(tip);
        }
        return genCodeLocationLinkFromStack(stack);
    }


    /**
     * 获取当前方法堆栈中调用目标类、方法的堆栈
     *
     * @param aimClass       目标类
     * @param aimMethodName  目标类方法，若 null 则忽略方法校验
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
            if (!isAimClass && ignoreSubClass) {
                try {
                    // 忽略子类
                    Class<?> clazz = Class.forName(stackClassName, false, Thread.currentThread().getContextClassLoader());
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
            boolean filterNot = !isAimClass && (skipCheckMethod || !aimMethodName.equals(stackTraceElement.getMethodName()));
            // 加个不能是 c/c++ 的代码（这个条件一般情况下永远为 true）
            if (filterNot && !stackTraceElement.isNativeMethod()) {
                return stackTraceElement;
            }
        }
        return null;
    }

}
