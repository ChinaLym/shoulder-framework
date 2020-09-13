package org.shoulder.security.code.img;


import org.shoulder.code.dto.ValidateCodeDTO;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDateTime;


/**
 * 图片验证码
 *
 * @author lym
 */
public class ImageCode extends ValidateCodeDTO {

    private BufferedImage image;

    public ImageCode(BufferedImage image, String code, Duration expireIn) {
        super(code, expireIn);
        this.image = image;
    }

    public ImageCode(BufferedImage image, String code, LocalDateTime expireTime) {
        super(code, expireTime);
        this.image = image;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
