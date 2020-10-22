package org.shoulder.core.i18n;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.shoulder.core.i18.ReloadableLocaleDirectoryMessageSource;
import org.springframework.context.NoSuchMessageException;

import java.util.Locale;

public class ShoulderResourceMessageTest {

    /**
     * 在代码中写中文容易乱码，受编译环境影响，故使用字母来代替
     */
    @Test
    public void testTranslate_zh() {
        ReloadableLocaleDirectoryMessageSource messageSource = new ReloadableLocaleDirectoryMessageSource();
        String result = messageSource.getMessage("shoulder.test.hi", null, Locale.CHINA);
        Assertions.assertThat(result).isEqualTo("hai");

        Assertions.assertThat(messageSource.getMessage("shoulder.test.hello", new String[]{"shoulder"}, Locale.CHINA))
            .isEqualTo("niHao,shoulder");
    }

    @Test
    public void testTranslate_en() {
        ReloadableLocaleDirectoryMessageSource messageSource = new ReloadableLocaleDirectoryMessageSource();
        String result = messageSource.getMessage("shoulder.test.hi", null, Locale.US);
        Assertions.assertThat(result).isEqualTo("hi");

        Assertions.assertThat(messageSource.getMessage("shoulder.test.hello", new String[]{"shoulder"}, Locale.US))
            .isEqualTo("hello, shoulder");
    }

    /**
     * 如果某个语言下没有对应的翻译，会报错
     */
    @Test(expected = NoSuchMessageException.class)
    public void testTranslate_zh_special() {
        ReloadableLocaleDirectoryMessageSource messageSource = new ReloadableLocaleDirectoryMessageSource();
        messageSource.getMessage("shoulder.test.cnSpecial", null, Locale.US);
    }

}
