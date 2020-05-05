package org.shoulder.security.code.img.config;

import org.shoulder.code.consts.ValidateCodeConsts;
import org.shoulder.code.propertities.ValidateCodeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 验证码配置项
 *
 * @author lym
 */
@ConfigurationProperties(prefix = ValidateCodeConsts.CONFIG_PREFIX + ".image")
public class ImageCodeProperties extends ValidateCodeProperties {

    /**
     * 图片宽
     */
    private int width = 120;

    /**
     * 图片高，推荐为宽的 1 / 3
     */
    private int height = 40;

    ImageCodeProperties(){
        // 默认长度为 4
        setLength(4);
        setParameterName(ValidateCodeConsts.IMAGE);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
