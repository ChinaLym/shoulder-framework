package org.shoulder.code.propertities;

import java.util.List;

/**
 * 验证码基础配置项
 *
 * @author lym
 * */
public abstract class ValidateCodeProperties {

    /**
     * 验证码长度
     */
    private int length = 6;

    /**
     * 验证码有效时间，默认 10 分钟
     */
    private int expireIn = 60 * 10;

    /** 请求中的参数名 */
    private String parameterName;

    /**
     * 需要校验验证码的 url 路径，支持通配符
     */
    private List<String> urls;

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(int expireIn) {
        this.expireIn = expireIn;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }


    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }
}
