package org.shoulder.security.code.img;

import org.shoulder.code.generator.ValidateCodeGenerator;
import org.shoulder.security.code.img.config.ImageCodeProperties;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;

import java.awt.image.BufferedImage;
import java.security.SecureRandom;
import java.util.Random;

/**
 * 默认的图片验证码生成器
 * 数字与字母
 * todo ThreadLocalRandom
 *
 * @author lym
 */
public class ImageCodeGenerator implements ValidateCodeGenerator, ImageValidateCodeType {

    private final String[] FONT_NAMES;
    /**
     * 生成的字符，只允许 0-9，与大写字母，除去容易与数字产生混淆的I、O
     */
    private final String[] CHARS = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H",
        "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    /**
     * 系统配置
     */
    private ImageCodeProperties imageCodeProperties;
    private Random random = new SecureRandom();

    public ImageCodeGenerator(ImageCodeProperties imageCodeProperties) {
        FONT_NAMES = imageCodeProperties.getFonts().toArray(new String[0]);
        this.imageCodeProperties = imageCodeProperties;
    }

    /**
     * 生成验证码图片
     */
    @Override
    public ImageCode generate(ServletWebRequest request) {
        // 1. 确定参数（图片宽、高，验证码长度）
        int width = ServletRequestUtils.getIntParameter(request.getRequest(), "width",
            imageCodeProperties.getWidth());
        int height = ServletRequestUtils.getIntParameter(request.getRequest(), "height",
            imageCodeProperties.getHeight());
        int codeLength = imageCodeProperties.getLength();

        // 生成随机验证码
        StringBuilder validateCode = new StringBuilder();
        for (int i = 0; i < imageCodeProperties.getLength(); i++) {
            int index = random.nextInt(CHARS.length);
            validateCode.append(CHARS[index]);
        }
        String validateCodeStr = validateCode.toString();

        // 获得一个画笔、图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = image.getGraphics();

        // 画背景
        graphics.setColor(nextRandomColor(200, 250));
        graphics.fillRoundRect(0, 0, width, height, 10, 10);
        // 干扰线
        graphics.setColor(nextRandomColor(110, 180));
        graphics.setFont(new Font("Times New Roman", Font.ITALIC, height - 10));
        int interferenceLineNum = 128;
        for (int i = 0; i < interferenceLineNum; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            graphics.drawLine(x, y, x + xl, y + yl);
        }

        // 画验证码
        graphics.setColor(nextRandomColor(40, 170));
        for (int i = 0; i < codeLength; i++) {
            final int offset = 5;
            int fontSize = height - 20 + random.nextInt(10);
            graphics.setFont(new Font(FONT_NAMES[random.nextInt(FONT_NAMES.length)], random.nextInt(3), fontSize));
            graphics.drawString(String.valueOf(validateCodeStr.charAt(i)), i * fontSize + offset, fontSize + random.nextInt(fontSize >> 1));
        }

        graphics.dispose();

        // 将图片返回
        return new ImageCode(image, validateCodeStr, imageCodeProperties.getEffectiveSeconds());
    }

    /**
     * 生成随机背景条纹
     * 获取随机颜色，亮度越大越接近白色，越低越接近黑色
     *
     * @param minLight 颜色的最低亮度
     * @param maxLight 颜色的最大亮度
     */
    private Color nextRandomColor(int minLight, int maxLight) {
        int rbgMax = 255;
        if (minLight < 0 || minLight > maxLight || maxLight > rbgMax) {
            throw new IllegalArgumentException("no such color(min=" + minLight + ",max=" + maxLight + ")!");
        }
        int r = minLight + random.nextInt(maxLight - minLight);
        int g = minLight + random.nextInt(maxLight - minLight);
        int b = minLight + random.nextInt(maxLight - minLight);
        return new Color(r, g, b);
    }

	/*void getFONTS(){
		    GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    String[] fontNames = e.getAvailableFontFamilyNames();
		    for (String fontName : fontNames) {
		        System.out.println(fontName);
		    }

	}*/

}
