package tw_six.demo.websocket;

import java.time.LocalDateTime;

/**
 * WebSocket消息数据模型 - 统一消息格式
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 定义WebSocket推送消息的标准格式，确保前后端消息结构一致。
 * 所有通过WebSocket推送的消息都使用此格式封装。
 * 
 * 【消息结构】
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │ WebSocketMessage                                                        │
 * ├─────────────────────────────────────────────────────────────────────────┤
 * │ - type: 消息类型 (VEHICLE_UPDATE, ALERT, SYSTEM)                        │
 * │ - timestamp: 消息时间戳                                                  │
 * │ - data: 消息数据载体 (Object类型，根据type不同而变化)                     │
 * │ - message: 人类可读的消息描述                                             │
 * └─────────────────────────────────────────────────────────────────────────┘
 * 
 * 【消息类型说明】
 * ┌──────────────────┬───────────────────────────────────────────────────────┐
 * │ 消息类型          │ 说明                                                  │
 * ├──────────────────┼───────────────────────────────────────────────────────┤
 * │ VEHICLE_UPDATE   │ 车辆位置更新，data为VehicleLocationData对象            │
 * │ VEHICLE_LIST     │ 车辆列表更新，data为List<VehicleLocationData>          │
 * │ ALERT            │ 告警通知，data为AlertData对象                          │
 * │ SYSTEM           │ 系统消息，data为String                                │
 * │ HEARTBEAT        │ 心跳检测，data为null                                  │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【JSON示例】
 * 车辆更新消息：
 * ```json
 * {
 *   "type": "VEHICLE_UPDATE",
 *   "timestamp": "2024-01-15T10:30:00",
 *   "data": {
 *     "id": 1,
 *     "carName": "京A10001",
 *     "latitude": 32.0650,
 *     "longitude": 118.7500,
 *     "speed": 45.5,
 *     "direction": 90.0,
 *     "status": "正常"
 *   },
 *   "message": "车辆位置已更新"
 * }
 * ```
 * 
 * 【使用示例】
 * ```java
 * // 创建车辆更新消息
 * VehicleLocationData vehicleData = new VehicleLocationData(...);
 * WebSocketMessage message = WebSocketMessage.vehicleUpdate(vehicleData);
 * 
 * // 创建告警消息
 * AlertData alertData = new AlertData(...);
 * WebSocketMessage alertMessage = WebSocketMessage.alert(alertData);
 * 
 * // 推送消息
 * messagingTemplate.convertAndSend("/topic/vehicles", message);
 * ```
 * 
 * 【前端解析示例】
 * ```javascript
 * stompClient.subscribe('/topic/vehicles', (message) => {
 *   const wsMessage = JSON.parse(message.body);
 *   switch(wsMessage.type) {
 *     case 'VEHICLE_UPDATE':
 *       updateVehicleOnMap(wsMessage.data);
 *       break;
 *     case 'ALERT':
 *       showNotification(wsMessage.message);
 *       break;
 *   }
 * });
 * ```
 * 
 * 【关联文件】
 * - VehicleLocationData.java: 车辆位置数据模型
 * - AlertData.java: 告警数据模型
 * - VehicleLocationPushService.java: 使用此类发送消息
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class WebSocketMessage {

    /**
     * 消息类型枚举
     * 定义所有可能的消息类型，便于前端根据类型进行不同处理
     */
    public enum MessageType {
        /** 车辆位置更新 - 单个车辆位置变化 */
        VEHICLE_UPDATE,
        
        /** 车辆列表更新 - 批量更新所有车辆位置 */
        VEHICLE_LIST,
        
        /** 告警通知 - 围栏告警、超速告警等 */
        ALERT,
        
        /** 系统消息 - 系统级别的通知 */
        SYSTEM,
        
        /** 心跳检测 - 保持连接活跃 */
        HEARTBEAT
    }

    /** 消息类型 */
    private MessageType type;
    
    /** 消息时间戳 */
    private LocalDateTime timestamp;
    
    /** 消息数据载体 - 根据消息类型不同，承载不同的数据对象 */
    private Object data;
    
    /** 人类可读的消息描述 */
    private String message;

    /**
     * 默认构造函数
     * 用于JSON反序列化
     */
    public WebSocketMessage() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 全参数构造函数
     * 
     * @param type 消息类型
     * @param data 消息数据
     * @param message 消息描述
     */
    public WebSocketMessage(MessageType type, Object data, String message) {
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.data = data;
        this.message = message;
    }

    // ==================== 静态工厂方法 ====================
    // 提供便捷的静态方法创建不同类型的消息

    /**
     * 创建车辆位置更新消息
     * 
     * @param vehicleData 车辆位置数据
     * @return WebSocket消息对象
     */
    public static WebSocketMessage vehicleUpdate(Object vehicleData) {
        return new WebSocketMessage(MessageType.VEHICLE_UPDATE, vehicleData, "车辆位置已更新");
    }

    /**
     * 创建车辆列表更新消息
     * 
     * @param vehicleList 车辆位置数据列表
     * @return WebSocket消息对象
     */
    public static WebSocketMessage vehicleList(Object vehicleList) {
        return new WebSocketMessage(MessageType.VEHICLE_LIST, vehicleList, "车辆列表已更新");
    }

    /**
     * 创建告警消息
     * 
     * @param alertData 告警数据
     * @param message 告警描述
     * @return WebSocket消息对象
     */
    public static WebSocketMessage alert(Object alertData, String message) {
        return new WebSocketMessage(MessageType.ALERT, alertData, message);
    }

    /**
     * 创建系统消息
     * 
     * @param message 系统消息内容
     * @return WebSocket消息对象
     */
    public static WebSocketMessage system(String message) {
        return new WebSocketMessage(MessageType.SYSTEM, null, message);
    }

    /**
     * 创建心跳消息
     * 
     * @return WebSocket心跳消息对象
     */
    public static WebSocketMessage heartbeat() {
        return new WebSocketMessage(MessageType.HEARTBEAT, null, "心跳检测");
    }

    // ==================== Getter和Setter方法 ====================

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 转换为JSON字符串
     * 用于日志输出和调试
     * 
     * @return JSON格式的字符串表示
     */
    @Override
    public String toString() {
        return String.format("WebSocketMessage{type=%s, timestamp=%s, message='%s'}", 
            type, timestamp, message);
    }
}
