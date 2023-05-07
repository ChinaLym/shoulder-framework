package org.shoulder.ext.config.provider.controller.spi;

import java.util.Collections;
import java.util.List;

/**
 * 默认的，返回空数据
 */
public class EmptyOldConfigDataQueryService implements OldConfigDataQueryService {

    @Override
    public List<Object> queryOldDataList(String tenant, String configTypeName) {
        return Collections.emptyList();
    }
}
