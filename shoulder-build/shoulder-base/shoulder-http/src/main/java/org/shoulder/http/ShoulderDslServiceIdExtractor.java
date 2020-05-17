package org.shoulder.http;

import java.net.URI;

/**
 * shoulder framework 方言版的 服务标识提取器
 * @author lym
 */
public class ShoulderDslServiceIdExtractor implements ServiceIdExtractor {

    /**
     * 从 uri 中提取服务标识
     *
     * @param uri 使用者发起 http 调用时填写的内容，如 (http://)order(:7000)/hello
     * @return 服务标识，如 order
     *      如果不是 shoulder dsl 形式，则返回 host, 如 http://order/hello 则返回 order
     */
    @Override
    public String extract(URI uri) {
        return uri.getHost();
    }

}
