package org.shoulder.ext.config;

import org.shoulder.core.util.StringUtils;
import org.shoulder.ext.config.domain.ConfigType;
import org.shoulder.ext.config.domain.PageInfo;
import org.shoulder.ext.config.domain.enums.TenantEnum;
import org.shoulder.ext.config.domain.model.ConfigData;
import org.shoulder.ext.config.service.ConfigManagerCoreService;
import org.shoulder.ext.config.service.ConfigQueryCoreService;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

class InMemoryConfigRepositoryService implements ConfigManagerCoreService, ConfigQueryCoreService {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<String, Map<String, ConfigData>> memoryStorage;

    {
        memoryStorage = new ConcurrentHashMap<>();
        memoryStorage.put(TenantEnum.DEFAULT, new ConcurrentHashMap<>());
    }


    @Override
    public void insert(ConfigData configData) {
        try {
            lock.writeLock().lock();
            memoryStorage.get(configData.getTenant()).put(configData.getBizId(), configData);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void update(ConfigData configData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void migration(ConfigData configData, boolean overwrite) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete(ConfigData configData) {
        try {
            lock.writeLock().lock();
            memoryStorage.get(configData.getTenant()).remove(configData.getBizId());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }


    @Nullable
    @Override
    public ConfigData queryByBizId(String bizId) {
        try {
            lock.readLock().lock();
            return memoryStorage.values().stream().map(m -> m.get(bizId)).filter(Objects::nonNull).findFirst().orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public ConfigData lockByBizId(String bizId) {
        return queryByBizId(bizId);
    }

    @Override
    public List<ConfigData> queryListByMultiCondition(String tenant, ConfigType configType,
                                                      @Nullable Map<String, String> filterCondition) {
        try {
            lock.readLock().lock();
            if (memoryStorage.get(tenant).size() == 0) {
                return new ArrayList<>();
            }
            return memoryStorage.get(tenant).values().stream().filter(c -> StringUtils.equals(c.getTenant(), tenant) && c.getConfigType() == configType).filter(
                    c -> match(c, filterCondition)).collect(
                    Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean match(ConfigData c, Map<String, String> filterCondition) {
        if (filterCondition == null) {
            return true;
        }
        AtomicBoolean match = new AtomicBoolean(true);
        filterCondition.forEach((k, v) -> {
            if (v == null) {
                match.set(match.get() && (c.getBusinessValue().get(k) == null));
            } else {
                match.set(match.get() && (v.equals(c.getBusinessValue().get(k))));
            }
        });
        return match.get();
    }

    @Override
    public PageInfo<ConfigData> queryPageByMultiCondition(String tenant, ConfigType configType,
                                                          @Nullable Map<String, String> filterCondition, int pageNum, int pageSize) {
        List<ConfigData> data = queryListByMultiCondition(tenant, configType, filterCondition);
        return PageInfo.success(data.stream().limit(pageSize).collect(Collectors.toList()), pageNum, pageSize, data.size());
    }
}