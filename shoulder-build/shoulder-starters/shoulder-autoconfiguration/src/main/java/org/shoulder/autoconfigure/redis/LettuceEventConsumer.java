package org.shoulder.autoconfigure.redis;

import io.lettuce.core.cluster.event.ClusterTopologyChangedEvent;
import io.lettuce.core.event.cluster.AdaptiveRefreshTriggeredEvent;
import io.lettuce.core.event.connection.*;
import io.lettuce.core.event.metrics.CommandLatencyEvent;

/**
 * redis 连接池事件监听
 *
 * @author lym
 */
public interface LettuceEventConsumer {

    /**
     * 命令耗时统计
     *
     * @param event event
     */
    default void onCommandLatencyEvent(CommandLatencyEvent event) {

    }

    /* ======================== clusterTopologyChanged ======================== */

    /**
     * 触发集群拓扑图刷新
     *
     * @param event event 启动时
     * @see io.lettuce.core.cluster.ClusterTopologyRefreshScheduler#indicateTopologyRefreshSignal() 调用处为触发原因
     */
    default void onClusterTopologyChangedEvent(ClusterTopologyChangedEvent event) {

    }

    /**
     * 自适应拓扑刷新
     *
     * @param event event
     * @see io.lettuce.core.cluster.ClusterTopologyRefreshScheduler 这里有触发原因
     */
    default void onAdaptiveRefreshTriggeredEvent(AdaptiveRefreshTriggeredEvent event) {

    }

    /* ======================== ConnectionEvent ======================== */

    /**
     * 初始化连接时
     *
     * @param event event
     */
    default void onConnectedEvent(ConnectedEvent event) {

    }

    /**
     * 连接可用时
     *
     * @param event event
     */
    default void onConnectionActivatedEvent(ConnectionActivatedEvent event) {

    }

    /**
     * 连接不可用时（连接销毁）
     *
     * @param event event
     */
    default void onConnectionDeactivatedEvent(ConnectionDeactivatedEvent event) {

    }

    /**
     * 连接不可用时（连接失败，连接断开）
     *
     * @param event event
     */
    default void onDisconnectedEvent(DisconnectedEvent event) {

    }

    /**
     * 每当重新连接失败时
     *
     * @param event event
     */
    default void onReconnectFailedEvent(ReconnectFailedEvent event) {

    }

}