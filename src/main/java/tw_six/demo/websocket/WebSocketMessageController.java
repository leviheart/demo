package tw_six.demo.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket消息处理控制器 - 客户端消息处理中心
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 处理客户端通过WebSocket发送的消息，实现双向通信。
 * 支持客户端订阅请求、心跳检测、消息确认等功能。
 * 
 * 【消息处理流程】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      客户端消息处理流程                                  │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │  客户端                    服务端                                       │
 * │    │                         │                                         │
 * │    │ ── SEND /app/subscribe ─▶│ 订阅请求                               │
 * │    │                         │ ── 处理订阅逻辑                          │
 * │    │◀── MESSAGE /topic/... ──│ 返回订阅确认                           │
 * │    │                         │                                         │
 * │    │ ── SEND /app/heartbeat ─▶│ 心跳检测                               │
 * │    │◀── MESSAGE /topic/... ──│ 返回心跳响应                           │
 * │    │                         │                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【消息映射说明】
 * ┌──────────────────────────┬───────────────────────────────────────────────┐
 * │ 客户端发送地址            │ 服务端处理方法                                │
 * ├──────────────────────────┼───────────────────────────────────────────────┤
 * │ /app/subscribe           │ handleSubscribe() - 处理订阅请求              │
 * │ /app/heartbeat           │ handleHeartbeat() - 处理心跳检测              │
 * │ /app/ack                 │ handleAck() - 处理消息确认                    │
 * │ /app/command             │ handleCommand() - 处理控制命令                │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【STOMP协议说明】
 * - @MessageMapping: 映射客户端发送的消息地址（自动添加/app前缀）
 * - @SendTo: 指定响应消息发送到的地址
 * - SimpMessageHeaderAccessor: 访问消息头信息（如会话ID）
 * 
 * 【连接状态管理】
 * 使用ConcurrentHashMap存储客户端连接信息：
 * - key: 会话ID
 * - value: 连接信息（连接时间、订阅主题等）
 * 
 * 【使用示例】
 * 前端发送订阅请求：
 * ```javascript
 * stompClient.send('/app/subscribe', {}, JSON.stringify({
 *   topics: ['vehicles', 'alerts'],
 *   clientId: 'client-001'
 * }));
 * ```
 * 
 * 【关联文件】
 * - WebSocketConfig.java: WebSocket配置
 * - WebSocketEventListener.java: 连接事件监听
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Controller
public class WebSocketMessageController {

    /** 时间格式化器 */
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** 连接计数器 - 统计当前连接数 */
    private static final AtomicInteger connectionCount = new AtomicInteger(0);
    
    /** 客户端连接信息存储 */
    private static final Map<String, ClientInfo> clientConnections = new ConcurrentHashMap<>();

    /**
     * 处理订阅请求
     * 
     * 客户端连接成功后，发送订阅请求指定要接收的消息类型。
     * 服务端记录订阅信息，并返回订阅确认。
     * 
     * @param subscribeRequest 订阅请求对象
     * @param headerAccessor 消息头访问器
     * @return 订阅确认消息
     */
    @MessageMapping("/subscribe")
    @SendTo("/topic/subscription")
    public WebSocketMessage handleSubscribe(SubscribeRequest subscribeRequest,
            SimpMessageHeaderAccessor headerAccessor) {
        
        // 获取会话ID
        String sessionId = headerAccessor.getSessionId();
        
        // 记录客户端信息
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setSessionId(sessionId);
        clientInfo.setClientId(subscribeRequest.getClientId());
        clientInfo.setTopics(subscribeRequest.getTopics());
        clientInfo.setConnectTime(LocalDateTime.now());
        
        clientConnections.put(sessionId, clientInfo);
        connectionCount.incrementAndGet();
        
        // 构建响应消息
        String message = String.format("客户端 %s 订阅成功，当前连接数: %d", 
            subscribeRequest.getClientId(), connectionCount.get());
        
        System.out.println(String.format("[WebSocket] %s", message));
        
        return WebSocketMessage.system(message);
    }

    /**
     * 处理心跳检测
     * 
     * 客户端定期发送心跳消息，保持连接活跃。
     * 服务端响应心跳，并更新客户端最后活跃时间。
     * 
     * @param headerAccessor 消息头访问器
     * @return 心跳响应消息
     */
    @MessageMapping("/heartbeat")
    @SendTo("/topic/heartbeat")
    public WebSocketMessage handleHeartbeat(SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        
        // 更新客户端最后活跃时间
        ClientInfo clientInfo = clientConnections.get(sessionId);
        if (clientInfo != null) {
            clientInfo.setLastHeartbeat(LocalDateTime.now());
        }
        
        return WebSocketMessage.heartbeat();
    }

    /**
     * 处理消息确认
     * 
     * 客户端收到重要消息后发送确认，服务端记录确认状态。
     * 用于确保消息送达（可靠性投递）。
     * 
     * @param ackRequest 确认请求
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/ack")
    public void handleAck(AckRequest ackRequest, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        
        System.out.println(String.format("[WebSocket] 收到消息确认 - 会话: %s, 消息ID: %s", 
            sessionId, ackRequest.getMessageId()));
    }

    /**
     * 处理控制命令
     * 
     * 接收客户端发送的控制命令（如请求立即推送数据）。
     * 
     * @param command 控制命令
     * @param headerAccessor 消息头访问器
     * @return 命令执行结果
     */
    @MessageMapping("/command")
    @SendTo("/topic/command")
    public WebSocketMessage handleCommand(CommandRequest command,
            SimpMessageHeaderAccessor headerAccessor) {
        
        String commandType = command.getCommand();
        System.out.println(String.format("[WebSocket] 收到控制命令: %s", commandType));
        
        String message;
        switch (commandType) {
            case "REFRESH":
                message = "数据刷新请求已接收";
                break;
            case "STATUS":
                message = String.format("当前连接数: %d", connectionCount.get());
                break;
            default:
                message = "未知命令: " + commandType;
        }
        
        return WebSocketMessage.system(message);
    }

    /**
     * 获取当前连接数
     * 
     * @return 当前连接数
     */
    public static int getConnectionCount() {
        return connectionCount.get();
    }

    /**
     * 移除客户端连接信息
     * 当客户端断开连接时调用
     * 
     * @param sessionId 会话ID
     */
    public static void removeClient(String sessionId) {
        if (clientConnections.remove(sessionId) != null) {
            connectionCount.decrementAndGet();
        }
    }

    // ==================== 内部类定义 ====================

    /**
     * 订阅请求对象
     */
    public static class SubscribeRequest {
        private String clientId;
        private String[] topics;

        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String[] getTopics() { return topics; }
        public void setTopics(String[] topics) { this.topics = topics; }
    }

    /**
     * 消息确认请求对象
     */
    public static class AckRequest {
        private String messageId;
        private String status;

        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * 控制命令请求对象
     */
    public static class CommandRequest {
        private String command;
        private Map<String, Object> params;

        public String getCommand() { return command; }
        public void setCommand(String command) { this.command = command; }
        public Map<String, Object> getParams() { return params; }
        public void setParams(Map<String, Object> params) { this.params = params; }
    }

    /**
     * 客户端连接信息
     */
    public static class ClientInfo {
        private String sessionId;
        private String clientId;
        private String[] topics;
        private LocalDateTime connectTime;
        private LocalDateTime lastHeartbeat;

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String[] getTopics() { return topics; }
        public void setTopics(String[] topics) { this.topics = topics; }
        public LocalDateTime getConnectTime() { return connectTime; }
        public void setConnectTime(LocalDateTime connectTime) { this.connectTime = connectTime; }
        public LocalDateTime getLastHeartbeat() { return lastHeartbeat; }
        public void setLastHeartbeat(LocalDateTime lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    }
}
