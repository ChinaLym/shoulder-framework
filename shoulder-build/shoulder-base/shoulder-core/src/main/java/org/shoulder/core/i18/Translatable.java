package org.shoulder.core.i18;

import org.springframework.context.MessageSourceResolvable;

/**
 * 可被翻译的
 * 对应 Spring 中的接口 {@link MessageSourceResolvable}，额外支持getCode返回单个，简化了使用
 * （推荐用枚举类实现）
 *
 * @author lym
 */
public interface Translatable extends MessageSourceResolvable {

    /**
     * Spring 中支持多个翻译 code，依次尝试翻译
     * 但实际中往往只需一个，由于 String[] 不如 String 类型通用，可以只使用单个，简化编码
     *
     * @return 多语言标识
     */
    default String getI18nKey() {
        return null;
    }

    /**
     * 返回要用来解决此消息的Code，依次尝试翻译。最后一个代码将是默认的Code
     *
     * @return 多语言标识
     * @implNote 改名
     */
    default String[] getI18nKeys() {
        return new String[]{getI18nKey()};
    }

    /**
     * 返回要用来解决此消息的Code，依次尝试翻译。最后一个代码将是默认的Code
     *
     * @return 多语言标识
     * @implNote 提升易用性
     */
    @Override
    default String[] getCodes() {
        return getI18nKeys();
    }

}
