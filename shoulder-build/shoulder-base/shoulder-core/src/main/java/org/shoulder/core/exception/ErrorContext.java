package org.shoulder.core.exception;

import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 以错误码形式保存错误堆栈
 * 考虑使用队列保存错误堆栈
 *
 * @author lym
 */
@Getter
@Setter
public class ErrorContext implements Serializable {
    private static final long serialVersionUID = 6299331063167879531L;

    /**
     * 分隔符
     */
    private static final String SPLIT = "|";

    /**
     * 默认堆栈大小
     */
    private static final int INIT_SIZE = 2;

    /**
     * 错误栈 0 根异常（可能为非shoulder体系，故code可空）
     */
    private List<ErrorCode> errorStack = new ArrayList<>(INIT_SIZE);

    /**
     * 默认构造方法。
     */
    public ErrorContext() {
        // 默认构造方法
    }

    /**
     * 构造方法。
     *
     * @param errorCode 错误码。
     */
    public ErrorContext(ErrorCode errorCode) {
        this.addError(errorCode);
    }

    /**
     * 获取当前错误对象。
     *
     * @return 当前错误码对象
     */
    @Nullable
    public ErrorCode fetchCurrentError() {
        if (errorStack != null && !errorStack.isEmpty()) {
            return errorStack.get(errorStack.size() - 1);
        }
        return null;
    }

    /**
     * 获取当前错误码。
     *
     * @return 当前错误码字符串
     */
    public String fetchCurrentErrorCode() {
        ErrorCode resultCode = fetchCurrentError();
        if (resultCode == null) {
            return null;
        } else {
            resultCode.getCode();
            return resultCode.getCode();
        }
    }

    /**
     * 获取原始错误对象。
     *
     * @return 原始错误码对象
     */
    public ErrorCode fetchRootError() {
        if (errorStack != null && !errorStack.isEmpty()) {
            return errorStack.get(0);
        }
        return null;
    }

    /**
     * 向堆栈中添加错误对象。
     *
     * @param error 错误码
     */
    public void addError(ErrorCode error) {
        if (errorStack == null) {
            errorStack = new ArrayList<>(INIT_SIZE);
        }
        errorStack.add(error);
    }

    /**
     * 获取字符串形式的错误码堆栈digest
     *
     * @return 错误码堆栈
     */
    public String fetchErrorCodeStackDigest() {
        int size = errorStack == null ? 0 : errorStack.size();
        if (size == 0) {
            return "";
        }
        // 0x12345678 + '|'
        StringBuilder stack = new StringBuilder(11 * errorStack.size());
        for (int i = size - 1; i >= 0; i--) {
            stack.append(errorStack.get(i)).append(SPLIT);
        }
        // 去掉最后多余的 '|'
        stack.setLength(stack.length() - 1);
        return stack.toString();
    }

}
