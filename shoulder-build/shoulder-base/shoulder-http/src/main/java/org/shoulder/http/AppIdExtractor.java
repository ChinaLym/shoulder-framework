package org.shoulder.http;

import jakarta.annotation.Nonnull;
import org.shoulder.http.exception.ResolveFailException;

import java.net.URI;

/**
 * 应用标识提取器，用于给其他功能提供基础能力
 *      服务发现、密钥交换
 *
 * @author lym
 */
public interface AppIdExtractor {

    /**
     * 从 uri 中提取应用标识
     *
     * @param uri 使用者发起 http 调用时填写的内容，如 (http://)order/hello
     * @return 应用标识，如 order
     * @throws ResolveFailException 解析失败，一般情况，只要合法就不会发生
     */
    @Nonnull
    String extract(URI uri) throws ResolveFailException;

}
