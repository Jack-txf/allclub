package com.feng.tim.ws.connection;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Description: 连接信息
 * @Author: txf
 * @Date: 2025/10/11
 */
public class ConnectionInfo {
    // ==================== 基础标识（不可变） ====================
    private final String connectionId; // 全局唯一连接ID（这里就采用uid了）
    private final String userId; // 关联用户ID（未登录可为null）
    private final ChannelId channelId; // Netty Channel唯一标识
    private final InetSocketAddress localAddress; // 本地地址
    private final InetSocketAddress remoteAddress; // 远程地址
    private final Instant createTime; // 创建时间（初始化时确定）

    // ==================== 状态管理（线程安全） ====================
    private final AtomicReference<ConnectionState> state = new AtomicReference<>(ConnectionState.INIT);
    private final AtomicReference<Instant> lastActiveTime = new AtomicReference<>(Instant.now()); // 最后活跃时间
    private final AtomicInteger retryCount = new AtomicInteger(0);

    // ==================== 业务标识（可扩展） ====================
    private final String clientType; // 客户端类型（如APP、WEB、DEVICE）

    private final Map<String, String> metadata = new ConcurrentHashMap<>(); // 自定义元数据（如设备型号、版本号）

    // ==================== 统计信息（原子操作） ====================
    private final AtomicInteger sendMsgCount = new AtomicInteger(0); // 发送消息数
    private final AtomicInteger receiveMsgCount = new AtomicInteger(0); // 接收消息数
    private final AtomicLong sendByteCount = new AtomicLong(0); // 发送字节数
    private final AtomicLong receiveByteCount = new AtomicLong(0); // 接收字节数

    // ==================== 关联对象 ====================
    private volatile Channel channel; // 关联的Netty Channel（可能为null，如断开后）

    // 构造器（私有，通过Builder创建）
    private ConnectionInfo(Builder builder) {
        this.connectionId = builder.connectionId;
        this.channelId = builder.channelId;
        this.localAddress = builder.localAddress;
        this.remoteAddress = builder.remoteAddress;
        this.createTime = Instant.now();
        this.clientType = builder.clientType;
        this.userId = builder.userId;
        this.channel = builder.channel;
        this.metadata.putAll(builder.metadata);
    }

    // ==================== 状态更新方法 ====================
    /** 更新连接状态（线程安全） */
    public boolean updateState(ConnectionState newState) {
        Objects.requireNonNull(newState);
        ConnectionState oldState = this.state.get();
        // 状态流转校验（例如：CLOSED状态不可再变更）
        if (oldState == ConnectionState.CLOSED) {
            return false;
        }
        return this.state.compareAndSet(oldState, newState);
    }

    /** 更新最后活跃时间（调用时机：发送/接收消息时） */
    public void updateLastActiveTime() {
        lastActiveTime.set(Instant.now());
        retryCount.set(0);
    }

    // ==================== 统计方法 ====================
    /** 累加发送消息统计 */
    public void incrementSendStats(int msgSize) {
        sendMsgCount.incrementAndGet();
        sendByteCount.addAndGet(msgSize);
        updateLastActiveTime();
    }

    /** 累加接收消息统计 */
    public void incrementReceiveStats(int msgSize) {
        receiveMsgCount.incrementAndGet();
        receiveByteCount.addAndGet(msgSize);
        updateLastActiveTime();
    }

    // ==================== 业务扩展方法 ====================
    /** 添加自定义元数据 */
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }

    /** 移除自定义元数据 */
    public void removeMetadata(String key) {
        metadata.remove(key);
    }

    // ==================== Getter（仅暴露必要字段，避免外部修改） ====================
    public String getConnectionId() { return connectionId; }
    public ChannelId getChannelId() { return channelId; }
    public InetSocketAddress getLocalAddress() { return localAddress; }
    public InetSocketAddress getRemoteAddress() { return remoteAddress; }
    public Instant getCreateTime() { return createTime; }
    public ConnectionState getState() { return state.get(); }
    public Instant getLastActiveTime() { return lastActiveTime.get(); }
    public String getClientType() { return clientType; }
    public String getUserId() { return userId; }
    public Map<String, String> getMetadata() { return metadata; } // 返回原始引用，允许外部遍历
    public int getSendMsgCount() { return sendMsgCount.get(); }
    public int getReceiveMsgCount() { return receiveMsgCount.get(); }
    public long getSendByteCount() { return sendByteCount.get(); }
    public long getReceiveByteCount() { return receiveByteCount.get(); }
    public Channel getChannel() { return channel; }

    // 仅允许内部设置Channel（如重连后更新）
    void setChannel(Channel channel) { this.channel = channel; }

    // ==================== 工具方法 ====================
    /** 判断连接是否活跃（已连接且Channel有效） */
    public boolean isActive() {
        return getState() == ConnectionState.CONNECTED
                && channel != null
                && channel.isActive();
    }

    @Override
    public String toString() {
        return String.format(
                "Connection{id=%s, remote=%s, state=%s, user=%s, send=%d, receive=%d}",
                connectionId, remoteAddress, state.get(), userId, sendMsgCount.get(), receiveMsgCount.get()
        );
    }

    public void incrementRetryCount() {
        if (retryCount.incrementAndGet() > 3) {
            updateState(ConnectionState.CLOSED);
        }
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    // ==================== 建造者模式（便于创建对象） ====================
    public static class Builder {
        private String connectionId;
        private ChannelId channelId;
        private InetSocketAddress localAddress;
        private InetSocketAddress remoteAddress;
        private String clientType;
        private String userId;
        private Channel channel;
        private Map<String, String> metadata = new ConcurrentHashMap<>();

        public Builder connectionId(String connectionId) {
            this.connectionId = connectionId;
            return this;
        }

        public Builder channel(Channel channel) {
            this.channel = channel;
            this.channelId = channel.id();
            this.localAddress = (InetSocketAddress) channel.localAddress();
            this.remoteAddress = (InetSocketAddress) channel.remoteAddress();
            return this;
        }

        public Builder clientType(String clientType) {
            this.clientType = clientType;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder metadata(Map<String, String> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }

        public ConnectionInfo build() {
            Objects.requireNonNull(connectionId, "connectionId must not be null");
            Objects.requireNonNull(channel, "channel must not be null");
            return new ConnectionInfo(this);
        }
    }
}
