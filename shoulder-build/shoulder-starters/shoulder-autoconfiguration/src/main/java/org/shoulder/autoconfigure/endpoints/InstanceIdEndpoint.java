package org.shoulder.autoconfigure.endpoints;

import org.shoulder.core.guid.InstanceIdProvider;
import org.shoulder.web.annotation.SkipResponseWrap;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * endpoint
 *
 * @author lym
 */
@Configuration
@Endpoint(id = "instance-id") // 不能驼峰
public class InstanceIdEndpoint {

    private final InstanceIdProvider instanceIdProvider;

    public InstanceIdEndpoint(InstanceIdProvider instanceIdProvider) {
        this.instanceIdProvider = instanceIdProvider;
    }

    //management.endpoints.web.exposure.include=instanceId

    @ReadOperation
    @SkipResponseWrap
    public Map<String, Object> endpoint() {
        Map<String, Object> map = new HashMap<>(1);
        map.put("instanceId", instanceIdProvider.getCurrentInstanceId());
        return map;
    }

}
