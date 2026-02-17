package tw_six.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类 - 实时通信核心配置
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 配置Spring WebSocket消息代理，实现服务器与客户端之间的实时双向通信。
 * 使用STOMP协议作为消息传输协议，支持发布/订阅模式。
 * 
 * 【技术架构】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                          WebSocket通信架构                              │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │  客户端(浏览器)                                                          │
 * │      │                                                                   │
 * │      │ STOMP over WebSocket                                              │
 * │      ▼                                                                   │
 * │  WebSocket端点 (/ws)                                                     │
 * │      │                                                                   │
 * │      ▼                                                                   │
 * │  消息代理 ─────────────────────────────────────┐                       │
 * │      │                                           │                       │
 * │      ├── /topic/* (广播消息)                      │                       │
 * │      │    └── /topic/vehicles (车辆位置更新)      │                       │
 * │      │    └── /topic/alerts (告警通知)            │                       │
 * │      │                                           │                       │
 * │      └── /app/* (客户端发送消息)                  │                       │
 * │           └── /app/subscribe (订阅请求)          │                       │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【消息流向说明】
 * 1. 服务器推送: 服务端 → /topic/vehicles → 所有订阅客户端
 * 2. 客户端订阅: 客户端 → /app/subscribe → 服务端处理
 * 3. 告警广播: 服务端 → /topic/alerts → 所有订阅客户端
 * 
 * 【STOMP协议简介】
 * STOMP (Simple Text Oriented Messaging Protocol) 是一种简单的消息协议。
 * - SUBSCRIBE: 订阅主题
 * - SEND: 发送消息
 * - MESSAGE: 接收消息
 * 
 * 【使用场景】
 * - 车辆位置实时更新推送
 * - 围栏告警实时通知
 * - 系统消息广播
 * 
 * 【前端对接】
 * 前端需要使用 sockjs-client 和 @stomp/stompjs 库进行连接：
 * ```javascript
 * import SockJS from 'sockjs-client';
 * import { Stomp } from '@stomp/stompjs';
 * 
 * const socket = new SockJS('http://localhost:8081/ws');
 * const stompClient = Stomp.over(socket);
 * stompClient.connect({}, () => {
 *   stompClient.subscribe('/topic/vehicles', (message) => {
 *     console.log('收到车辆更新:', JSON.parse(message.body));
 *   });
 * });
 * ```
 * 
 * 【关联文件】
 * - WebSocketMessage.java: 消息数据模型
 * - VehicleLocationPushService.java: 车辆位置推送服务
 * - AlertPushService.java: 告警推送服务
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * 
     * 消息代理负责管理消息的路由和分发。
     * - /topic: 用于服务器向客户端广播消息（一对多）
     * - /queue: 用于点对点消息（一对一）
     * 
     * @param config 消息代理注册器，用于配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的内存消息代理
        // /topic: 广播消息，所有订阅该主题的客户端都能收到
        // /queue: 点对点消息，只有特定用户能收到
        config.enableSimpleBroker("/topic", "/queue");
        
        // 设置客户端发送消息的前缀
        // 客户端发送到 /app 开头的消息会被路由到消息处理方法
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀（用于点对点消息）
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     * 
     * STOMP端点是客户端连接WebSocket的入口地址。
     * 使用SockJS提供WebSocket降级支持，兼容不支持WebSocket的浏览器。
     * 
     * @param registry STOMP端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点
        registry.addEndpoint("/ws")
            // 允许跨域访问（开发环境允许所有来源）
            // 生产环境应配置具体的允许域名
            .setAllowedOriginPatterns("*")
            // 启用SockJS降级支持
            // 当浏览器不支持WebSocket时，自动降级为HTTP长轮询等方式
            .withSockJS();
    }
}
