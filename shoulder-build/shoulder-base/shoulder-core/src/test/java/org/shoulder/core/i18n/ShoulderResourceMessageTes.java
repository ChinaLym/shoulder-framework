package org.shoulder.core.i18n;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.core.i18.ShoulderMessageSource;

import java.util.Locale;

public class ShoulderResourceMessageTes {

    /**
     * 在代码中写中文容易乱码，受编译环境影响，故使用字母来代替
     */
    @Test
    public void calculateAllFilenames_zh(){
        ShoulderMessageSource messageSource = new ShoulderMessageSource();
        String result = messageSource.getMessage("shoulder.test.hi", null, Locale.CHINA);
        Assertions.assertThat(result).isEqualTo("hai");

        Assertions.assertThat(messageSource.getMessage("shoulder.test.hello", new String[]{"shoulder"}, Locale.CHINA))
            .isEqualTo("niHao,shoulder");
    }

    @Test
    public void calculateAllFilenames_en(){
        ShoulderMessageSource messageSource = new ShoulderMessageSource();
        String result = messageSource.getMessage("shoulder.test.hi", null, Locale.US);
        Assertions.assertThat(result).isEqualTo("hi");

        Assertions.assertThat(messageSource.getMessage("shoulder.test.hello", new String[]{"shoulder"}, Locale.US))
            .isEqualTo("hello, shoulder");
    }

}
