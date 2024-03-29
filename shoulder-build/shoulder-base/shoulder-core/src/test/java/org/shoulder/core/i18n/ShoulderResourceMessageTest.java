package org.shoulder.core.i18n;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.shoulder.core.i18.ReloadableLocaleDirectoryMessageSource;

import java.util.Locale;

/**
 * 涉及资源文件加载，IDEA 要保证 resource 目录被标记为 Test Root Resource
 */
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
     * 如果某个语言下没有对应的翻译，会 fallback 到默认语言
     */
    @Test
    public void testTranslate_zh_special() {
        ReloadableLocaleDirectoryMessageSource messageSource = new ReloadableLocaleDirectoryMessageSource();
        String tr = messageSource.getMessage("shoulder.test.cnSpecial", null, Locale.US);
        Assertions.assertThat(tr).isEqualTo("this is a zh_CN special message");
    }

}
