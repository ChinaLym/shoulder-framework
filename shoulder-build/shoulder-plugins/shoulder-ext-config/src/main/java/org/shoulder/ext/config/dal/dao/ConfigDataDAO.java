package org.shoulder.ext.config.dal.dao;

import org.apache.ibatis.annotations.Mapper;
import org.shoulder.ext.config.dal.dataobject.ConfigDataDO;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * DAO
 *
 * @author lym
 */
@Mapper
public interface ConfigDataDAO {


    ConfigDataDO querySingleByBizId(String bizId, boolean needLock);


    List<ConfigDataDO> queryListByMultiCondition(@Nonnull ConfigDataDO configDataDO, @Nullable List<String> bizIdList,
                                                 @Nullable Integer offset, @Nullable Integer size);

    long countByMultiCondition(@Nonnull ConfigDataDO configDataDO, @Nullable List<String> bizIdList);

    int insert(@Nonnull ConfigDataDO configDataDO);

    int updateByBizIdAndVersion(@Nonnull ConfigDataDO configDataDO);


    int updateDeleteVersionByBizIdAndVersion(@Nonnull ConfigDataDO configDataDO);
}