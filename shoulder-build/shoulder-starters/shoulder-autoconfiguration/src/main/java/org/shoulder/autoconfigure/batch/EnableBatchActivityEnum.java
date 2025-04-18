package org.shoulder.autoconfigure.batch;

import org.shoulder.batch.progress.BatchActivityEnum;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 指定 Shoulder 对哪些枚举类字典生成查询接口
 * （可方便前端开发下拉选择输入框、模糊搜索等）
 *
 * @author lym
 * @see BatchActivityEnum
 * @see BatchActivityEnumPackageScanRegistrar
 * @Deprecated 暂未启用
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({BatchActivityEnumPackageScanRegistrar.class})
@Documented
public @interface EnableBatchActivityEnum {

    /**
     * 将自动扫描指定包路径下的 {@link BatchActivityEnum}
     *
     * @return 要激活的包路径
     */
    String[] value();

}