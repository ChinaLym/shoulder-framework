package org.shoulder.core.i18;

import org.springframework.context.MessageSourceResolvable;

/**
 * 可被翻译的
 * 对应 Spring 中的接口 {@link MessageSourceResolvable}，本类仅简化了使用（支持getCode返回单个），也可以直接继承 Spring 中的接口
 * （推荐用枚举类实现）
 * 自动适配上下文，不需要传入语种，语种取值顺序：从当前用户或请求头中获取语言标识、其次设置的默认语言、其次系统语言
 *
 * @author lym
 */
public interface Translatable extends MessageSourceResolvable {

    /**
     * 对应的语言标识
     *
     * @return 语言标识
     */
    default String getCode() {
        return null;
    }

    /**
     * Spring 中支持多个语言标识，依次尝试翻译
     * 但实际中往往只需一个，由于 String[] 不如 String 类型通用，可以只使用 {@link #getCode()}，但并不强制
     *
     * @return 语言标识
     * @implNote 提升易用性
     */
    @Override
    default String[] getCodes() {
        return new String[]{getCode()};
    }

}
