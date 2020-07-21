package org.shoulder.http;

import java.net.URI;

/**
 * 应用标识提取器
 * @author lym
 */
public interface AppIdExtractor {

    /**
     * 从 uri 中提取应用标识
     * @param uri 使用者发起 http 调用时填写的内容，如 (http://)order/hello
     * @return 应用标识，如 order
     */
    String extract(URI uri);

}