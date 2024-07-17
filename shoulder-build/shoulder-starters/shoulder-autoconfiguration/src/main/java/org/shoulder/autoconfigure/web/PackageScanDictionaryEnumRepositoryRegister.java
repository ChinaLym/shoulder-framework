package org.shoulder.autoconfigure.web;

import org.shoulder.core.dictionary.model.DictionaryItemEnum;
import org.shoulder.core.dictionary.spi.DictionaryEnumStore;
import org.shoulder.core.exception.CommonErrorCodeEnum;
import org.shoulder.core.log.Logger;
import org.shoulder.core.log.ShoulderLoggers;
import org.shoulder.core.util.AssertUtils;
import org.shoulder.core.util.ContextUtils;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 供使用者根据扫描包路径注册枚举类
 *
 * @author lym
 */
@SuppressWarnings("unchecked")
public class PackageScanDictionaryEnumRepositoryRegister implements DictionaryEnumRepositoryCustomizer {

    private final Logger logger = ShoulderLoggers.SHOULDER_CONFIG;

    private final Set<String> toScanPackages;

    public PackageScanDictionaryEnumRepositoryRegister(Collection<? extends CharSequence> toScanPackages) {
        AssertUtils.notNull(toScanPackages, CommonErrorCodeEnum.CODING);
        this.toScanPackages = toScanPackages.stream().map(CharSequence::toString).collect(Collectors.toSet());
    }

    @Override
    public void customize(DictionaryEnumStore dictionaryEnumStore) {
        toScanPackages.forEach(packageName -> {
            AtomicInteger count = new AtomicInteger();
            ContextUtils.loadClassInPackage(packageName,
                    clazz -> clazz.isEnum() && DictionaryItemEnum.class.isAssignableFrom(clazz),
                    clazz -> {
                        dictionaryEnumStore.register((Class<? extends Enum<? extends DictionaryItemEnum<?, ?>>>) clazz);
                        logger.info("register " + clazz.getName() + " to DefaultDictionaryEnumStore.");
                        count.incrementAndGet();
                    });
            logger.info("register " + count.get() + " classes DefaultDictionaryEnumStore.");

        });

    }

}
