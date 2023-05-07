package org.shoulder.ext.config.provider.controller.spi;

import java.util.List;

public interface OldConfigDataQueryService {

    List<Object> queryOldDataList(String tenant, String configTypeName);

}
