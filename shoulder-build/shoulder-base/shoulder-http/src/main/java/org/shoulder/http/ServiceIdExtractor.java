package org.shoulder.http;

import java.net.URI;

/**
 * 服务标识提取器
 * @author lym
 */
public interface ServiceIdExtractor {

    /**
     * 从 uri 中提取服务标识
     * @param uri 使用者发起 http 调用时填写的内容，如 (http://)order/hello
     * @return 服务标识，如 order
     */
    String extract(URI uri);

}
