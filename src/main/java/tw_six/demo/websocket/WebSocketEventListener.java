package tw_six.demo.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WebSocket事件监听器 - 连接生命周期管理
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 监听WebSocket连接的建立和断开事件，实现连接生命周期管理。
 * 用于统计在线用户数、记录连接日志、清理断开连接的资源等。
 * 
 * 【事件类型】
 * ┌──────────────────────────┬───────────────────────────────────────────────┐
 * │ 事件类型                  │ 触发时机                                      │
 * ├──────────────────────────┼───────────────────────────────────────────────┤
 * │ SessionConnectedEvent    │ 客户端成功建立WebSocket连接                   │
 * │ SessionDisconnectEvent   │ 客户端断开WebSocket连接                       │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【连接生命周期】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                      WebSocket连接生命周期                               │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │                                                                         │
 * │  客户端发起连接                                                          │
 * │       │                                                                 │
 * │       ▼                                                                 │
 * │  ┌─────────────┐                                                       │
 * │  │ HTTP握手    │  ← SockJS降级支持                                     │
 * │  └─────────────┘                                                       │
 * │       │                                                                 │
 * │       ▼                                                                 │
 * │  ┌─────────────┐                                                       │
 * │  │ WebSocket   │  ← 协议升级                                           │
 * │  │ 连接建立    │                                                       │
 * │  └─────────────┘                                                       │
 * │       │                                                                 │
 * │       │  触发 SessionConnectedEvent                                    │
 * │       │  ─── 记录连接信息                                              │
 * │       │  ─── 更新在线计数                                              │
 * │       ▼                                                                 │
 * │  ┌─────────────┐                                                       │
 * │  │ 正常通信    │  ← 心跳检测、消息收发                                  │
 * │  └─────────────┘                                                       │
 * │       │                                                                 │
 * │       │  客户端断开或超时                                               │
 * │       ▼                                                                 │
 * │  ┌─────────────┐                                                       │
 * │  │ 连接关闭    │                                                       │
 * │  └─────────────┘                                                       │
 * │       │                                                                 │
 * │       │  触发 SessionDisconnectEvent                                   │
 * │       │  ─── 清理连接信息                                              │
 * │       │  ─── 更新在线计数                                              │
 * │       ▼                                                                 │
 * │  资源释放完成                                                           │
 * │                                                                         │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【使用场景】
 * 1. 在线用户统计：实时显示当前在线客户端数量
 * 2. 连接日志记录：记录连接建立和断开的时间
 * 3. 资源清理：断开连接时清理该连接相关的资源
 * 4. 安全审计：记录异常断开情况
 * 
 * 【日志输出示例】
 * ```
 * [WebSocket连接] 新连接建立 - 会话ID: abc123, 时间: 2024-01-15 10:30:00
 * [WebSocket连接] 当前在线数: 5
 * [WebSocket断开] 连接关闭 - 会话ID: abc123, 原因: 正常断开, 时间: 2024-01-15 11:00:00
 * [WebSocket断开] 当前在线数: 4
 * ```
 * 
 * 【关联文件】
 * - WebSocketConfig.java: WebSocket配置
 * - WebSocketMessageController.java: 消息处理控制器
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Component
public class WebSocketEventListener {

    /** 时间格式化器 */
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** 总连接计数器 - 统计历史总连接数 */
    private static int totalConnections = 0;
    
    /** 总断开计数器 - 统计历史总断开数 */
    private static int totalDisconnections = 0;

    /**
     * 处理WebSocket连接建立事件
     * 
     * 当客户端成功建立WebSocket连接时触发此方法。
     * 用于记录连接信息、更新在线统计等。
     * 
     * @param event 连接建立事件对象
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // 获取STOMP消息头访问器
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // 获取会话ID
        String sessionId = headerAccessor.getSessionId();
        
        // 更新统计
        totalConnections++;
        
        // 记录日志
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        System.out.println(String.format(
            "[WebSocket连接] 新连接建立 - 会话ID: %s, 时间: %s", 
            sessionId, timestamp
        ));
        System.out.println(String.format(
            "[WebSocket连接] 当前在线数: %d, 历史总连接: %d", 
            WebSocketMessageController.getConnectionCount(), totalConnections
        ));
    }

    /**
     * 处理WebSocket连接断开事件
     * 
     * 当客户端断开WebSocket连接时触发此方法。
     * 用于清理连接资源、更新在线统计等。
     * 
     * @param event 连接断开事件对象
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // 获取STOMP消息头访问器
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // 获取会话ID
        String sessionId = headerAccessor.getSessionId();
        
        // 从连接管理器中移除
        WebSocketMessageController.removeClient(sessionId);
        
        // 更新统计
        totalDisconnections++;
        
        // 记录日志
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);
        System.out.println(String.format(
            "[WebSocket断开] 连接关闭 - 会话ID: %s, 时间: %s", 
            sessionId, timestamp
        ));
        System.out.println(String.format(
            "[WebSocket断开] 当前在线数: %d, 历史总断开: %d", 
            WebSocketMessageController.getConnectionCount(), totalDisconnections
        ));
    }

    /**
     * 获取统计信息
     * 
     * @return 统计信息字符串
     */
    public static String getStatistics() {
        return String.format(
            "当前在线: %d, 历史连接: %d, 历史断开: %d",
            WebSocketMessageController.getConnectionCount(),
            totalConnections,
            totalDisconnections
        );
    }
}
