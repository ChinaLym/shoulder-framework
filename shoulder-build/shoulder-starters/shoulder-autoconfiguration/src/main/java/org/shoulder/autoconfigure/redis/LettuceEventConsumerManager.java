package org.shoulder.autoconfigure.redis;

import io.lettuce.core.cluster.event.ClusterTopologyChangedEvent;
import io.lettuce.core.event.Event;
import io.lettuce.core.event.cluster.AdaptiveRefreshTriggeredEvent;
import io.lettuce.core.event.connection.*;
import io.lettuce.core.event.metrics.CommandLatencyEvent;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Consumer;

/**
 * redis 连接池事件监听
 *
 * @author lym
 * @see org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory#createClient() 可以拿到 client 再次封裝
 * spring boot 会在初始化 redisfactory Bean 时创建连接
 */
public class LettuceEventConsumerManager implements Consumer<Event> {

    private final List<LettuceEventConsumer> eventConsumers;

    public LettuceEventConsumerManager(@NotNull @NotEmpty List<LettuceEventConsumer> eventConsumers) {
        this.eventConsumers = eventConsumers;
    }

    @Override
    public void accept(Event event) {
        if (event instanceof CommandLatencyEvent) {
            eventConsumers.forEach(c -> c.onCommandLatencyEvent((CommandLatencyEvent) event));
        } else if (event instanceof ClusterTopologyChangedEvent) {
            eventConsumers.forEach(c -> c.onClusterTopologyChangedEvent((ClusterTopologyChangedEvent) event));
        } else if (event instanceof AdaptiveRefreshTriggeredEvent) {
            eventConsumers.forEach(c -> c.onAdaptiveRefreshTriggeredEvent((AdaptiveRefreshTriggeredEvent) event));
        } else if (event instanceof ConnectedEvent) {
            eventConsumers.forEach(c -> c.onConnectedEvent((ConnectedEvent) event));
        } else if (event instanceof ConnectionActivatedEvent) {
            eventConsumers.forEach(c -> c.onConnectionActivatedEvent((ConnectionActivatedEvent) event));
        } else if (event instanceof ConnectionDeactivatedEvent) {
            eventConsumers.forEach(c -> c.onConnectionDeactivatedEvent((ConnectionDeactivatedEvent) event));
        } else if (event instanceof DisconnectedEvent) {
            eventConsumers.forEach(c -> c.onDisconnectedEvent((DisconnectedEvent) event));
        } else if (event instanceof ReconnectFailedEvent) {
            eventConsumers.forEach(c -> c.onReconnectFailedEvent((ReconnectFailedEvent) event));
        }
    }
}