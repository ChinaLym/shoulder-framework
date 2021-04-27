package org.shoulder.ext.config.dal.dao;

import org.shoulder.ext.config.dal.dataobject.ConfigDataDO;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @author lym
 */
@Repository
public interface ConfigDataDAO {


    ConfigDataDO querySingleByBizId(String bizId, boolean needLock);


    List<ConfigDataDO> queryListByMultiCondition(@Nonnull ConfigDataDO configDataDO, @Nullable List<String> bizIdList,
                                                 @Nullable Integer offset, @Nullable Integer size);

    long countByMultiCondition(@Nonnull ConfigDataDO configDataDO, @Nullable List<String> bizIdList);

    int insert(@Nonnull ConfigDataDO configDataDO);

    int updateByBizIdAndVersion(@Nonnull ConfigDataDO configDataDO);


    int updateDeleteVersionByBizIdAndVersion(@Nonnull ConfigDataDO configDataDO);
}