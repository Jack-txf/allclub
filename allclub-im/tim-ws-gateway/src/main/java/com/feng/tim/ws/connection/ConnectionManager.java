package com.feng.tim.ws.connection;

import com.feng.tim.ws.config.WsGatewayConfig;
import com.feng.tim.ws.net.NetUtil;
import com.feng.tim.ws.redis.RedisKey;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接信息管理器（线程安全）
 */
@Slf4j
public class ConnectionManager {
    // 核心存储：connectionId [可以看做uid ] -> Connection
    private final Map<String, ConnectionInfo> connections = new ConcurrentHashMap<>();
    // 索引：channelId -> connectionId（通过Channel快速查找连接）
    private final Map<ChannelId, String> channelIndex = new ConcurrentHashMap<>();
    /*
        connectionId 就可以是 uid了嘛
        基于Redis的ws中心，是一个hash数据结构，表示用户连接的是哪一个ws网关，由于不同结点之间的消息传递是通过RocketMQ的
        不同网关监听的topic也是不同的。比如说[ws1 -- im-topic1], [ws2 -- im-topic2], [ws3 -- im-topic3]
        【注意事项】：不同的ws网关结点一定不能监听同一个topic！！！！！！！！！
        key的格式: ws-center:[uid%20]
        value:
          - field(用户uid) value(网关信息)
          - field1('1') value1('192.168.1.1:10000|im-topic1')
          - field2('2') value2('192.168.1.1:10000|im-topic1')
     */
    private final RedisTemplate<String, Object> redisTemplate; // 与redis交互【因为ws-gateway不止一个节点】
    private final WsGatewayConfig conf;

    public ConnectionManager( RedisTemplate<String, Object> redisTemplate, WsGatewayConfig conf ) {
        this.redisTemplate = redisTemplate;
        this.conf = conf;
    }

    // 查看用户在本地，还是其他结点，还是离线
    public String getUserNodeState(String uid) {
        if ( uid == null ) return UserNodeState.Local;
        if ( connections.get(uid) != null ) return UserNodeState.Unknown;
        if ( redisTemplate == null ) {
            log.warn("redisTemplate为空，无法确定！");
            return UserNodeState.Offline;
        }
        Object remote = redisTemplate.opsForHash().get(RedisKey.wsCenterKey(uid), uid);
        return remote == null ? UserNodeState.Offline : remote.toString();
    }

    /**
     * 连接管理中心与redis交互的都在这儿了
     */
    private boolean judgeTemplate() {
        if ( redisTemplate == null ) {
            log.warn("redisTemplate为空，无法操作！");
            return false;
        }
        return true;
    }
    // 1. 注册到redis
    private boolean registerToRedis( ConnectionInfo connection ) {
        return registerToRedis(connection.getUserId());
    }
    private boolean registerToRedis( String uid ) {
        if ( judgeTemplate() ) {
            String key = RedisKey.wsCenterKey(uid);
            try {
                redisTemplate.opsForHash().put(key,
                        uid, NetUtil.getIp() + "|" + conf.getListenTopic());
            } catch (Exception e) {
                log.error("redis服务器异常 {}", e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }
    // 2. 删除redis
    private boolean removeFromRedis( ConnectionInfo connection ) {
        return removeFromRedis(connection.getUserId());
    }
    private boolean removeFromRedis( String uid ) {
        if ( judgeTemplate() ) {
            String key = RedisKey.wsCenterKey(uid);
            try {
                redisTemplate.opsForHash().delete(key, uid);
            } catch (Exception e) {
                log.error("redis服务器异常 {}", e.getMessage());
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 注册新连接
     */
    public void register(ConnectionInfo connection) {
        Objects.requireNonNull(connection);
        String connectionId = connection.getConnectionId();
        ChannelId channelId = connection.getChannelId();
        String userId = connection.getUserId();

        // 注册到主存储
        connections.put(connectionId, connection);
        // 维护channel索引
        channelIndex.put(channelId, connectionId);
        //  注册到redis
        registerToRedis(connection);
    }
    public void register(Long uid, Channel channel) {
        // 1.创建connectionInfo
        ConnectionInfo.Builder builder = new ConnectionInfo.Builder();
        ConnectionInfo connectionInfo = builder.connectionId(String.valueOf(uid))
                .userId(String.valueOf(uid))
                .channel(channel)
                .build();
        register(connectionInfo);
    }

    //============================================ 本地连接 ====================================
    /**
     * 根据connectionId获取连接
     */
    public ConnectionInfo getByConnectionId(String connectionId) {
        return connections.get(connectionId);
    }

    /**
     * 根据Channel获取连接（Netty事件中常用）
     */
    public ConnectionInfo getByChannel(Channel channel) {
        String connectionId = channelIndex.get(channel.id());
        return connectionId != null ? connections.get(connectionId) : null;
    }

    /**
     * 根据userId获取该用户的所有连接
     */
    public ConnectionInfo getByUserId(String userId) {
        return connections.get(userId);
    }

    /**
     * 移除连接（连接断开时调用）
     */
    public void remove(ConnectionInfo connection) {
        if ( connection != null ) {
            String connectionId = connection.getConnectionId();
            ChannelId channelId = connection.getChannelId();
            String userId = connection.getUserId();
            // 从主存储移除
            connections.remove(connectionId);
            // 从channel索引移除
            channelIndex.remove(channelId);
            // 更新连接状态为已关闭
            connection.updateState(ConnectionState.CLOSED);
            // 删除redis
            removeFromRedis(connection);
        }
    }

    /**
     * 获取所有连接（用于统计或广播）
     */
    public Collection<ConnectionInfo> getAllConnections() {
        return connections.values();
    }

    /**
     * 获取连接总数
     */
    public int getTotalCount() {
        return connections.size();
    }

}