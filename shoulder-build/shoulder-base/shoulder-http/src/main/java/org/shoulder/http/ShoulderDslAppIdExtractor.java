package org.shoulder.http;

import jakarta.annotation.Nonnull;
import java.net.URI;

/**
 * shoulder framework 方言版的 应用标识提取器
 *
 * @author lym
 */
public class ShoulderDslAppIdExtractor implements AppIdExtractor {

    /**
     * 从 uri 中提取应用标识
     *
     * @param uri 使用者发起 http 调用时填写的内容，如 (http://)order(:7000)/hello
     * @return 应用标识，如 order
     * 如果不是 shoulder dsl 形式，则返回 host, 如 http://order/hello 则返回 order
     */
    @Nonnull
    @Override
    public String extract(URI uri) {
        return uri.getHost();
    }

}
