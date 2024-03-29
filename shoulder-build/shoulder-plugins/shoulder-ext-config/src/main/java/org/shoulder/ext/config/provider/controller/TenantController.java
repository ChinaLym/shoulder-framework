package org.shoulder.ext.config.provider.controller;

import org.shoulder.core.dto.response.ListResult;
import org.shoulder.ext.common.constant.ShoulderExtConstants;
import org.shoulder.ext.config.provider.dto.TenantInfoDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;

/**
 * @author lym
 * @deprecated 后续租户ext专门提供
 */
@Controller
@RequestMapping(ShoulderExtConstants.TENANT_URL_PREFIX)
public class TenantController {

    private static final ListResult<TenantInfoDTO> TENANT_LIST;

    static {
        List<TenantInfoDTO> tenantInfoDTOList = Collections.singletonList(new TenantInfoDTO("DEFAULT", "默认"));
        TENANT_LIST = ListResult.of(tenantInfoDTOList);
    }

    /**
     * Query tenant list result.
     *
     * @return the result
     */
    @GetMapping("queryTenantList")
    @ResponseBody
    public ListResult<TenantInfoDTO> queryTenantList() {
        return TENANT_LIST;
    }

}
