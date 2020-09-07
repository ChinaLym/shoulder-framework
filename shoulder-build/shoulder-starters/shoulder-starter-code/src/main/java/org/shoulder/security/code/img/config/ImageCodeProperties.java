package org.shoulder.security.code.img.config;

import org.shoulder.code.consts.ValidateCodeConsts;
import org.shoulder.code.propertities.BaseValidateCodeProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;

/**
 * 验证码配置项
 *
 * @author lym
 */
@Validated
@ConfigurationProperties(prefix = ValidateCodeConsts.CONFIG_PREFIX + ".image")
public class ImageCodeProperties extends BaseValidateCodeProperties {

    /**
     * 图片宽
     */
    @Min(1)
    private int width = 120;

    /**
     * 图片高，推荐为宽的 1 / 3
     */
    @Min(1)
    private int height = 40;

    /**
     * 希望使用的验证码的字体
     * 填写系统中可用的字体名称
     * 这里仅举例，实际中需要使用者切换为合适的字体，避免因为系统中缺少对应的字体导致验证码生成失败，或者字体未授权
     */
    @NotEmpty
    private List<String> fonts = Arrays.asList("方正舒体", "华文彩云", "华文琥珀", "华文新魏", "幼圆",
        "微软雅黑", "楷体", "Agency FB", "Bradley Hand ITC", "Copperplate Gothic Light");


    ImageCodeProperties() {
        // 默认长度为 4 个字符
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

    public List<String> getFonts() {
        return fonts;
    }

    public void setFonts(List<String> fonts) {
        this.fonts = fonts;
    }
}
