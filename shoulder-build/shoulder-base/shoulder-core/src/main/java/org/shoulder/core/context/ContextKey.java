/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2024 All Rights Reserved.
 */
package org.shoulder.core.context;

/**
 * 上下文key约束，都需要实现该接口，方便管理统计
 *
 * @author lym
 */
public interface ContextKey<T> {
    String getKeyName();

    Class<T> valueClass();

}
