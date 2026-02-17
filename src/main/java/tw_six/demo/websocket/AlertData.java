package tw_six.demo.websocket;

import java.time.LocalDateTime;

/**
 * 告警数据模型 - WebSocket传输专用
 * 
 * ═══════════════════════════════════════════════════════════════════════════
 * 【功能概述】
 * 定义告警数据的传输格式，用于WebSocket推送各类告警通知。
 * 包括围栏告警、超速告警、设备异常等多种告警类型。
 * 
 * 【告警类型说明】
 * ┌──────────────────┬───────────────────────────────────────────────────────┐
 * │ 告警类型          │ 触发条件                                              │
 * ├──────────────────┼───────────────────────────────────────────────────────┤
 * │ GEOFENCE_ENTER   │ 车辆进入围栏区域                                      │
 * │ GEOFENCE_EXIT    │ 车辆离开围栏区域                                      │
 * │ OVERSPEED        │ 车辆速度超过限制值                                    │
 * │ DEVICE_OFFLINE   │ 设备离线超过指定时间                                  │
 * │ LOW_BATTERY      │ 设备电量低于阈值                                      │
 * │ EMERGENCY        │ 紧急情况（司机触发）                                  │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【告警级别说明】
 * ┌──────────────────┬───────────────────────────────────────────────────────┐
 * │ 告警级别          │ 说明                                                  │
 * ├──────────────────┼───────────────────────────────────────────────────────┤
 * │ INFO             │ 信息提示，无需紧急处理                                │
 * │ WARNING          │ 警告，需要关注但不紧急                                │
 * │ ERROR            │ 错误，需要尽快处理                                    │
 * │ CRITICAL         │ 严重，需要立即处理                                    │
 * └──────────────────┴───────────────────────────────────────────────────────┘
 * 
 * 【JSON示例】
 * ```json
 * {
 *   "alertId": "ALT-20240115-001",
 *   "alertType": "GEOFENCE_EXIT",
 *   "alertLevel": "WARNING",
 *   "carName": "京A10001",
 *   "fenceName": "仓库围栏",
 *   "message": "车辆 京A10001 已离开围栏 仓库围栏",
 *   "latitude": 32.0650,
 *   "longitude": 118.7500,
 *   "speed": 45.5,
 *   "timestamp": "2024-01-15T10:30:00",
 *   "handled": false
 * }
 * ```
 * 
 * 【前端使用示例】
 * ```javascript
 * // 订阅告警消息
 * stompClient.subscribe('/topic/alerts', (message) => {
 *   const wsMessage = JSON.parse(message.body);
 *   const alert = wsMessage.data; // AlertData对象
 *   
 *   // 根据告警级别显示不同样式的通知
 *   const notificationType = {
 *     'INFO': 'info',
 *     'WARNING': 'warning',
 *     'ERROR': 'error',
 *     'CRITICAL': 'error'
 *   }[alert.alertLevel];
 *   
 *   ElMessage({
 *     type: notificationType,
 *     message: alert.message,
 *     duration: alert.alertLevel === 'CRITICAL' ? 0 : 5000
 *   });
 *   
 *   // 在地图上显示告警位置
 *   showAlertOnMap(alert);
 * });
 * ```
 * 
 * 【关联文件】
 * - WebSocketMessage.java: 消息封装类
 * - AlertPushService.java: 创建并发送告警数据
 * - FenceAlert.java: 数据库实体类（数据来源）
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class AlertData {

    /**
     * 告警类型枚举
     * 定义所有可能的告警类型
     */
    public enum AlertType {
        /** 围栏进入告警 - 车辆进入监控围栏 */
        GEOFENCE_ENTER,
        
        /** 围栏离开告警 - 车辆离开监控围栏 */
        GEOFENCE_EXIT,
        
        /** 超速告警 - 车辆速度超过限制 */
        OVERSPEED,
        
        /** 设备离线告警 - GPS设备失去连接 */
        DEVICE_OFFLINE,
        
        /** 低电量告警 - 设备电量不足 */
        LOW_BATTERY,
        
        /** 紧急告警 - 司机触发紧急情况 */
        EMERGENCY
    }

    /**
     * 告警级别枚举
     * 定义告警的严重程度
     */
    public enum AlertLevel {
        /** 信息 - 一般性提示 */
        INFO,
        
        /** 警告 - 需要关注 */
        WARNING,
        
        /** 错误 - 需要处理 */
        ERROR,
        
        /** 严重 - 需要立即处理 */
        CRITICAL
    }

    /** 告警ID - 唯一标识，格式：ALT-日期-序号 */
    private String alertId;
    
    /** 告警类型 */
    private AlertType alertType;
    
    /** 告警级别 */
    private AlertLevel alertLevel;
    
    /** 相关车辆名称 */
    private String carName;
    
    /** 相关围栏名称（围栏告警专用） */
    private String fenceName;
    
    /** 告警描述信息 */
    private String message;
    
    /** 告警发生位置 - 纬度 */
    private Double latitude;
    
    /** 告警发生位置 - 经度 */
    private Double longitude;
    
    /** 触发告警时的速度 */
    private Double speed;
    
    /** 告警时间 */
    private LocalDateTime timestamp;
    
    /** 是否已处理 */
    private Boolean handled;

    /**
     * 默认构造函数
     */
    public AlertData() {
        this.timestamp = LocalDateTime.now();
        this.handled = false;
    }

    /**
     * 创建围栏告警
     * 
     * @param alertId 告警ID
     * @param type 告警类型（进入/离开）
     * @param carName 车辆名称
     * @param fenceName 围栏名称
     * @param latitude 纬度
     * @param longitude 经度
     * @return AlertData对象
     */
    public static AlertData geofenceAlert(String alertId, AlertType type, 
            String carName, String fenceName, Double latitude, Double longitude) {
        AlertData alert = new AlertData();
        alert.setAlertId(alertId);
        alert.setAlertType(type);
        alert.setAlertLevel(AlertLevel.WARNING);
        alert.setCarName(carName);
        alert.setFenceName(fenceName);
        alert.setLatitude(latitude);
        alert.setLongitude(longitude);
        
        String action = type == AlertType.GEOFENCE_ENTER ? "进入" : "离开";
        alert.setMessage(String.format("车辆 %s 已%s围栏 %s", carName, action, fenceName));
        
        return alert;
    }

    /**
     * 创建超速告警
     * 
     * @param alertId 告警ID
     * @param carName 车辆名称
     * @param speed 实际速度
     * @param limit 速度限制
     * @param latitude 纬度
     * @param longitude 经度
     * @return AlertData对象
     */
    public static AlertData overspeedAlert(String alertId, String carName,
            Double speed, Double limit, Double latitude, Double longitude) {
        AlertData alert = new AlertData();
        alert.setAlertId(alertId);
        alert.setAlertType(AlertType.OVERSPEED);
        alert.setAlertLevel(AlertLevel.ERROR);
        alert.setCarName(carName);
        alert.setSpeed(speed);
        alert.setLatitude(latitude);
        alert.setLongitude(longitude);
        alert.setMessage(String.format("车辆 %s 超速！当前速度 %.1f km/h，限速 %.1f km/h", 
            carName, speed, limit));
        
        return alert;
    }

    // ==================== Getter和Setter方法 ====================

    public String getAlertId() {
        return alertId;
    }

    public void setAlertId(String alertId) {
        this.alertId = alertId;
    }

    public AlertType getAlertType() {
        return alertType;
    }

    public void setAlertType(AlertType alertType) {
        this.alertType = alertType;
    }

    public AlertLevel getAlertLevel() {
        return alertLevel;
    }

    public void setAlertLevel(AlertLevel alertLevel) {
        this.alertLevel = alertLevel;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getFenceName() {
        return fenceName;
    }

    public void setFenceName(String fenceName) {
        this.fenceName = fenceName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getHandled() {
        return handled;
    }

    public void setHandled(Boolean handled) {
        this.handled = handled;
    }

    /**
     * 转换为字符串
     * 用于日志输出和调试
     * 
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return String.format("AlertData{id='%s', type=%s, level=%s, car='%s', message='%s'}", 
            alertId, alertType, alertLevel, carName, message);
    }
}
