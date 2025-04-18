package org.shoulder.autoconfigure.batch;

import org.shoulder.batch.progress.BatchActivityEnum;
import org.shoulder.batch.progress.BatchActivityRepository;
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
 * @Deprecated 暂未启用
 */
@SuppressWarnings("unchecked")
public class PackageScanBatchActivityEnumRepositoryRegister implements BatchActivityEnumRepositoryCustomizer {

    private final Logger logger = ShoulderLoggers.SHOULDER_CONFIG;

    private final Set<String> toScanPackages;

    public PackageScanBatchActivityEnumRepositoryRegister(Collection<? extends CharSequence> toScanPackages) {
        AssertUtils.notNull(toScanPackages, CommonErrorCodeEnum.CODING);
        this.toScanPackages = toScanPackages.stream().map(CharSequence::toString).collect(Collectors.toSet());
    }

    @Override
    public void customize(BatchActivityRepository repository) {
        String repoName = repository.getClass().getSimpleName();
        toScanPackages.forEach(packageName -> {
            AtomicInteger count = new AtomicInteger();
            ContextUtils.loadClassInPackage(packageName,
                    clazz -> clazz.isEnum() && BatchActivityEnum.class.isAssignableFrom(clazz),
                    clazz -> {
                        repository.register((Class<? extends BatchActivityEnum<?>>) clazz, clazz.getSimpleName());
                        logger.debug("register '{}' to {}.", clazz.getName(), repoName);
                        count.incrementAndGet();
                    });
            logger.info("register {} classes to {}.", count.get(), repoName);
        });

    }

}
