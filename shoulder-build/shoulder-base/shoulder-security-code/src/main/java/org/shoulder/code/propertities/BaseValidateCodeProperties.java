package org.shoulder.code.propertities;

import javax.validation.constraints.Min;
import java.util.List;

/**
 * 验证码基础配置项
 *
 * @author lym
 */
public abstract class BaseValidateCodeProperties {

    /**
     * 验证码长度，默认6个字符
     */
    @Min(1)
    private int length = 6;

    /**
     * 验证码有效秒数，默认 10 分钟
     */
    private int effectiveSeconds = 60 * 10;

    /**
     * 请求中的参数名
     */
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

    public int getEffectiveSeconds() {
        return effectiveSeconds;
    }

    public void setEffectiveSeconds(int effectiveSeconds) {
        this.effectiveSeconds = effectiveSeconds;
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
